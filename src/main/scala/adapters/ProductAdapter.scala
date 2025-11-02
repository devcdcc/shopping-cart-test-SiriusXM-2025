package com.siriusxm.cart
package adapters

import domain.*

import errors.*

import cats.data.EitherT
import cats.effect.Async
import cats.syntax.all.*
import io.circe.parser.decode
import org.http4s.{Method, Request, Status, Uri}
import org.http4s.client.Client

trait ProductAdapter[F[_]]:
  def getProductById(productId: String): EitherT[F, CartError, CartProduct]
end ProductAdapter


object ProductAdapter:

  def live[F[_] : Async](client: Client[F]): ProductAdapter[F] =
    new ProductAdapter[F]:
      private val UrlPrefix = "https://raw.githubusercontent.com/mattjanks16/shopping-cart-test-data/main"

      def getProductById(productId: String): EitherT[F, CartError, CartProduct] =
        val url = s"$UrlPrefix/$productId.json"
        for
          uri <- EitherT.fromEither[F](
            Uri.fromString(url).leftMap(_ => CartError.UnknownError)
          )
          product <- EitherT(
            client.run(Request[F](method = Method.GET, uri = uri)).use { resp =>
              resp.status match
                case Status.NotFound =>
                  CartError.ElementDoesNotExistsError.asLeft[CartProduct].pure[F]
                case s if s.code < 400 =>
                  resp
                    .as[String]
                    .attempt
                    .map {
                      case Left(_) => Left(CartError.UnknownError)
                      case Right(body) =>
                        decode[CartProduct](body).leftMap(_ => CartError.CartDecodingError)
                    }

                case _ =>
                  CartError.UnknownError.asLeft[CartProduct].pure[F]
            }
          )
        yield product
end ProductAdapter
