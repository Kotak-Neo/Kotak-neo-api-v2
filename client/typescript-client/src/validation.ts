import { ApiValueError } from "./exceptions";
import {
  EXCHANGE_SEGMENT,
  PRODUCT,
  ORDER_TYPE,
  SEGMENT_LIMITS,
  EXCHANGE_LIMITS,
  PRODUCT_LIMITS,
} from "./settings";

function nonEmpty(val: unknown, name: string): void {
  if (val === undefined || val === null || String(val).trim() === "") {
    throw new ApiValueError(`${name} is mandatory`);
  }
}

const VALIDITY_VALUES = ["DAY", "IOC"];
const PLACE_ORDER_TT = ["B", "S", "Buy", "Sell"];
const MARGIN_TT = ["B", "S", "Buy", "Sell", "sell", "buy"];

export function validatePlaceOrder(p: {
  exchange_segment: string;
  product: string;
  price: string;
  order_type: string;
  quantity: string;
  validity: string;
  trading_symbol: string;
  transaction_type: string;
}): void {
  Object.entries(p).forEach(([k, v]) => nonEmpty(v, k));
  if (!(p.exchange_segment in EXCHANGE_SEGMENT))
    throw new ApiValueError(`invalid exchange_segment: ${p.exchange_segment}`);
  if (!(p.product in PRODUCT)) throw new ApiValueError(`invalid product: ${p.product}`);
  if (!(p.order_type in ORDER_TYPE))
    throw new ApiValueError(`invalid order_type: ${p.order_type}`);
  if (!VALIDITY_VALUES.includes(p.validity))
    throw new ApiValueError("Invalid validity. Allowed values are DAY, IOC.");
  if (!PLACE_ORDER_TT.includes(p.transaction_type))
    throw new ApiValueError("Invalid transaction type. Allowed values are B or Buy, S or Sell.");
}

export function validateCancelOrder(orderId: string): void {
  nonEmpty(orderId, "order_id");
}

export function validateOrderHistory(orderId: string): void {
  nonEmpty(orderId, "order_id");
}

export function validateMargin(p: {
  exchange_segment: string;
  price: string;
  order_type: string;
  product: string;
  quantity: string;
  instrument_token: string;
  transaction_type: string;
}): void {
  Object.entries(p).forEach(([k, v]) => nonEmpty(v, k));
  if (!(p.exchange_segment in EXCHANGE_SEGMENT))
    throw new ApiValueError(`invalid exchange_segment: ${p.exchange_segment}`);
  if (!(p.product in PRODUCT))
    throw new ApiValueError(`invalid product: ${p.product}`);
  if (!(p.order_type in ORDER_TYPE))
    throw new ApiValueError(`invalid order_type: ${p.order_type}`);
  if (!MARGIN_TT.includes(p.transaction_type))
    throw new ApiValueError("Invalid transaction type. Allowed values are B or Buy, S or Sell.");
}

export function validateLimits(segment: string, exchange: string, product: string): void {
  if (!SEGMENT_LIMITS.includes(segment))
    throw new ApiValueError(`invalid segment: ${segment}`);
  if (!EXCHANGE_LIMITS.includes(exchange))
    throw new ApiValueError(`invalid exchange: ${exchange}`);
  if (!PRODUCT_LIMITS.includes(product))
    throw new ApiValueError(`invalid product: ${product}`);
}
