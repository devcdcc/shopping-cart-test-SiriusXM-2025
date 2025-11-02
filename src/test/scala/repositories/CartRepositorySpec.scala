package com.siriusxm.cart
package repositories


import domain.*

import cats.effect.IO
import munit.CatsEffectSuite

final class CartRepositorySpec extends CatsEffectSuite {

  test("createOrReturnCart: should return empty cart for non-existing session") {
    // given
    val sessionId = "createOrReturnCart-new"
    val expected = Cart(sessionId, List.empty)
    for {
      repo <- CartRepository.inMemory[IO]
      // when
      result <- repo.getOrCreateCart(sessionId)
      // then
    } yield assertEquals(result, expected)
  }

  test("createOrReturnCart: should return existing data") {
    // given
    val sessionId = "createOrReturnCart-existing"
    val expected = Cart(
      sessionId = sessionId,
      cartItems = List(
        CartItem("Corn Flakes", Price(2.52), 2),
        CartItem("Cheerios", Price(8.43), 1)
      )
    )
    for {
      repo <- CartRepository.inMemory[IO]
      // when
      _ <- repo.addOrIncrementProductQuantity(sessionId, "Corn Flakes", Price(2.52), 2)
      _ <- repo.addOrIncrementProductQuantity(sessionId, "Cheerios", Price(8.43), 1)
      result <- repo.getOrCreateCart(sessionId)
    } yield assertEquals(result, expected)
  }

  test("addOrIncrementProductQuantity: should create and modify data") {
    // given
    val sessionId = "addOrIncrementProductQuantity-existing"
    val firstExpected = Cart(
      sessionId = sessionId,
      cartItems = List(CartItem("Corn Flakes", Price(2.52), 2))
    )
    val lastExpected = Cart(
      sessionId = sessionId,
      cartItems = List(CartItem("Corn Flakes", Price(2.52), 5))
    )
    for {
      repo <- CartRepository.inMemory[IO]
      // when
      _ <- repo.addOrIncrementProductQuantity(sessionId, "Corn Flakes", Price(2.52), 2)
      firstSave <- repo.getOrCreateCart(sessionId)
      _ <- repo.addOrIncrementProductQuantity(sessionId, "Corn Flakes", Price(2.52), 3)
      lastSave <- repo.getOrCreateCart(sessionId)
      // then
    } yield {
      assertEquals(firstSave, firstExpected)
      assertEquals(lastSave, lastExpected)
    }
  }
}