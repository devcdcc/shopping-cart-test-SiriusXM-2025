package com.siriusxm.cart
package domain

import io.circe.{Codec, Decoder, Encoder}
import io.circe.generic.semiauto.*
import scala.math.BigDecimal.RoundingMode


opaque type Price = Double

object Price:
  def apply(v: Double): Price = BigDecimal(v).setScale(2, RoundingMode.HALF_UP).toDouble


  given Decoder[Price] = Decoder.decodeDouble.map(Price(_))

  given Encoder[Price] = Encoder.encodeDouble.contramap[Price](_.toDouble)

  extension (p: Price) def toDouble: Double = p
end Price

final case class CartProduct(title: String, price: Double) derives Codec.AsObject

final case class CartItem(title: String, price: Price, quantity: Int) derives Codec.AsObject

final case class Cart(sessionId: String, cartItems: List[CartItem]) derives Codec.AsObject

final case class CartSummary(
                              sessionId: String,
                              cartItems: List[CartItem],
                              subTotal: Price,
                              tax: Price,
                              total: Price
                            ) derives Codec.AsObject


object JsonCodecs:
  given Codec[CartProduct] = deriveCodec
  given Codec[CartItem]    = deriveCodec
  given Codec[Cart]        = deriveCodec
  given Codec[CartSummary] = deriveCodec