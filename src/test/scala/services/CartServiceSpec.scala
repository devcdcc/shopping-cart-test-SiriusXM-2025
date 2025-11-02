package com.siriusxm.cart
package services


import adapters.ProductAdapter
import domain.*
import errors.CartError
import repositories.CartRepository

import cats.effect.IO
import munit.CatsEffectSuite
import org.http4s.ember.client.EmberClientBuilder

final class CartServiceSpec extends CatsEffectSuite {

  private val client = ResourceSuiteLocalFixture(
    "httpClient",
    EmberClientBuilder.default[IO].build
  )

  override def munitFixtures = List(client)

  private def mkService: IO[CartService[IO]] =
    for {
      repo <- CartRepository.inMemory[IO]
      padp = ProductAdapter.live[IO](client())
    } yield CartService.live[IO](padp, repo)

  test("getCart: should get an empty cart for a new SessionId") {
    // given
    val sessionId = "getCart-new"
    val expected = Cart(sessionId = sessionId, cartItems = List.empty)

    for {
      svc <- mkService
      // when
      result <- svc.getCart(sessionId)
      // then
    } yield assertEquals(result, expected)
  }

  test("getCart: should return the existing cart assigned to SessionId") {
    // given
    val sessionId = "getCart-exist"
    val expected = Cart(
      sessionId = sessionId,
      cartItems = List(
        CartItem("Corn Flakes", Price(2.52), 2),
        CartItem("Cheerios", Price(8.43), 1)
      )
    )

    for {
      svc <- mkService
      _ <- svc.addProduct(sessionId, 2, "cornflakes").value
      _ <- svc.addProduct(sessionId, 1, "cheerios").value
      // when
      result <- svc.getCart(sessionId)
      // then
    } yield assertEquals(result, expected)
  }

  test("addProduct: should create a new item if product does not exist in cart") {
    // given
    val sessionId = "addProduct-new"
    val expected = Cart(
      sessionId = sessionId,
      cartItems = List(CartItem("Corn Flakes", Price(2.52), 2))
    )

    for {
      svc <- mkService
      _ <- svc.addProduct(sessionId, 2, "cornflakes").value
      // when
      result <- svc.getCart(sessionId)
      // then
    } yield assertEquals(result, expected)
  }

  test("addProduct: should increment product quantity for existing product") {
    // given
    val sessionId = "addProduct-existing"
    val expected = Cart(
      sessionId = sessionId,
      cartItems = List(CartItem("Corn Flakes", Price(2.52), 5))
    )

    for {
      svc <- mkService
      _ <- svc.addProduct(sessionId, 2, "cornflakes").value
      _ <- svc.addProduct(sessionId, 3, "cornflakes").value
      // when
      result <- svc.getCart(sessionId)
      // then
    } yield assertEquals(result, expected)
  }

  test("addProduct: should fail with ElementDoesNotExistsError") {
    // given
    val sessionId = "addProduct-error-404"

    for {
      svc <- mkService
      // when
      result <- svc.addProduct(sessionId, 2, "cornflakes-404").value
      // then
    } yield assertEquals(result, Left(CartError.ElementDoesNotExistsError))
  }

  test("getSummary: should fail with EmptyCartError") {
    // given
    val sessionId = "getSummary-new"
    for {
      svc <- mkService
      // when
      result <- svc.getCartSummary(sessionId).value
      // then
    } yield assertEquals(result, Left(CartError.EmptyCartError))
  }

  test("getSummary: should return Cart Summary") {
    // given
    val sessionId = "getSummary-existing"
    val expected = CartSummary(
      sessionId,
      List(
        CartItem("Corn Flakes", Price(2.52), 2),
        CartItem("Weetabix", Price(9.98), 1)
      ),
      Price(15.02),
      Price(1.88),
      Price(16.90)
    )

    for {
      svc <- mkService
      _ <- svc.addProduct(sessionId, 2, "cornflakes").value
      _ <- svc.addProduct(sessionId, 1, "weetabix").value
      // when
      result <- svc.getCartSummary(sessionId).value
      // then
    } yield assertEquals(result, Right(expected))
  }
}