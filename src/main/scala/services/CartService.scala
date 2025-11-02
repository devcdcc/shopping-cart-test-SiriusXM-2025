package com.siriusxm.cart
package services


import adapters.ProductAdapter
import domain.*
import errors.*
import repositories.CartRepository

import cats.Monad
import cats.data.EitherT

trait CartService[F[_]]:
  def getCart(sessionId: String): F[Cart]
  def addProduct(sessionId: String, quantity: Int, productId: String): EitherT[F, CartError, Unit]
  def getCartSummary(sessionId: String): EitherT[F, CartError, CartSummary]
end CartService


object CartService:
  private final class CartServiceLive[F[_]: Monad](
                                            productAdapter: ProductAdapter[F],
                                            repository: CartRepository[F]
                                          ) extends CartService[F]:

    override def getCart(sessionId: String): F[Cart] =
      repository.getOrCreateCart(sessionId)

    override def addProduct(
                             sessionId: String,
                             quantity: Int,
                             productId: String
                           ): EitherT[F, CartError, Unit] =
      for {
        productInfo <- productAdapter.getProductById(productId)
        _           <- EitherT.liftF(
          repository.addOrIncrementProductQuantity(
            sessionId,
            productInfo.title,
            Price(productInfo.price),
            quantity
          )
        )
      } yield ()

    override def getCartSummary(sessionId: String): EitherT[F, CartError, CartSummary] =
      for {
        cart <- EitherT.liftF(repository.getOrCreateCart(sessionId))
        _    <- EitherT.cond[F](cart.cartItems.nonEmpty, (), CartError.EmptyCartError)
        subTotal = cart.cartItems.foldLeft(0.0) { (acc, item) =>
          acc + (item.price.toDouble * item.quantity)
        }
        tax   = subTotal * TaxRate
        total = subTotal + tax
      } yield CartSummary(cart.sessionId, cart.cartItems, Price(subTotal), Price(tax), Price(total))
  end CartServiceLive


  private[services] val TaxRate: Double = 12.5 / 100.0

  def live[F[_]: Monad](
                         productAdapter: ProductAdapter[F],
                         repository: CartRepository[F]
                       ): CartService[F] =
    new CartServiceLive(productAdapter, repository)
end CartService
