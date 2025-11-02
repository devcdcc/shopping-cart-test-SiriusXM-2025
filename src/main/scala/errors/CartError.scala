package com.siriusxm.cart
package errors

enum CartError:
  case ElementDoesNotExistsError
  case ConnectionError
  case CartDecodingError
  case EmptyCartError
  case UnknownError
end CartError
