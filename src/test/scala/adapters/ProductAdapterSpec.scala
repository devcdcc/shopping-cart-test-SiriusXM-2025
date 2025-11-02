package com.siriusxm.cart
package adapters

import domain.*
import errors.*

import cats.effect.IO
import munit.CatsEffectSuite
import org.http4s._
import org.http4s.client.Client
import org.http4s.dsl.io._
import org.http4s.HttpRoutes

final class ProductAdapterSpec extends CatsEffectSuite {
  // mocks
  private val mockRoutes: HttpRoutes[IO] = HttpRoutes.of[IO] {
    case req if req.method == Method.GET &&
      req.uri.path.renderString.endsWith("/cornflakes.json") =>
      Ok("""{"title":"Corn Flakes","price":2.52}""")
    case req if req.method == Method.GET &&
      req.uri.path.renderString.endsWith("/cornflakes-error.json") =>
      NotFound()
    case _ =>
      InternalServerError("unexpected url in test")
  }

  private val mockClient: Client[IO] =
    Client.fromHttpApp(mockRoutes.orNotFound)

  private def adapter = ProductAdapter.live[IO](mockClient)

  test("getProductById should return error when product is not found") {
    // given
    val wrongProductId = "cornflakes-error"
    val expected = Left(CartError.ElementDoesNotExistsError)
    // when
    val result = adapter.getProductById(wrongProductId)
    // then
    result.value.map { result =>
      assertEquals(result, expected)
    }
  }

  test("getProductById should return CartProduct for valid productId") {
    // given
    val productId = "cornflakes"
    val expected = Right(CartProduct("Corn Flakes", 2.52))
    // when
    val result = adapter.getProductById(productId)
    // then
    result.value.map { result =>
      assertEquals(result, expected)
    }
  }
}