package com.siriusxm.cart
package repositories


import domain.*
import cats.effect.kernel.Sync
import cats.effect.kernel.Ref
import cats.syntax.all.*

trait CartRepository[F[_]]:
  def getOrCreateCart(sessionId: String): F[Cart]

  def addOrIncrementProductQuantity(sessionId: String, title: String, price: Price, quantity: Int): F[Unit]
end CartRepository

object CartRepository:

  final private class CartRepositoryLive[F[_] : Sync](dataMapRef: Ref[F, Map[String, Cart]])
    extends CartRepository[F] {

    private def addItemReducer(
                                cartItems: List[CartItem],
                                title: String,
                                price: Price,
                                quantity: Int
                              ): List[CartItem] = {
      val newCartItem = CartItem(title, price, quantity)
      cartItems.find(_.title == title) match
        case Some(_) =>
          cartItems.map { oldItem =>
            if (oldItem.title == newCartItem.title)
              newCartItem.copy(quantity = quantity + oldItem.quantity)
            else oldItem
          }
        case None =>
          cartItems :+ newCartItem
    }

    override def getOrCreateCart(sessionId: String): F[Cart] =
      dataMapRef.modify { data =>
        val cart = data.getOrElse(sessionId, Cart(sessionId = sessionId, cartItems = List.empty))
        (data.updated(sessionId, cart), cart)
      }

    override def addOrIncrementProductQuantity(
                                                sessionId: String,
                                                title: String,
                                                price: Price,
                                                quantity: Int
                                              ): F[Unit] =
      dataMapRef.update { data =>
        val oldCart = data.getOrElse(sessionId, Cart(sessionId = sessionId, cartItems = List.empty))
        val newCart = oldCart.copy(
          cartItems = addItemReducer(oldCart.cartItems, title, price, quantity)
        )
        data.updated(sessionId, newCart)
      }
  }

  def inMemory[F[_] : Sync]: F[CartRepository[F]] =
    Ref.of[F, Map[String, Cart]](Map.empty).map(dataMapRef => new CartRepositoryLive[F](dataMapRef))
end CartRepository
