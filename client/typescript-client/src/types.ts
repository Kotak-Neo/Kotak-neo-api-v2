export interface PlaceOrderRequest {
  exchange_segment: string;
  product: string;
  price: string;
  order_type: string;
  quantity: string;
  validity: string;
  trading_symbol: string;
  transaction_type: string;
  amo?: string;
  disclosed_quantity?: string;
  market_protection?: string;
  pf?: string;
  trigger_price?: string;
  tag?: string;
  scrip_token?: string;
  square_off_type?: string;
  stop_loss_type?: string;
  stop_loss_value?: string;
  square_off_value?: string;
  last_traded_price?: string;
  trailing_stop_loss?: string;
  trailing_sl_value?: string;
}

export interface ModifyOrderRequest {
  order_id: string;
  price: string;
  order_type: string;
  quantity: string;
  validity: string;
  instrument_token?: string;
  exchange_segment?: string;
  product?: string;
  trading_symbol?: string;
  transaction_type?: string;
  trigger_price?: string;
  dd?: string;
  market_protection?: string;
  disclosed_quantity?: string;
  filled_quantity?: string;
  amo?: string;
}

export interface MarginRequiredRequest {
  exchange_segment: string;
  price: string;
  order_type: string;
  product: string;
  quantity: string;
  instrument_token: string;
  transaction_type: string;
  trigger_price?: string;
  broker_name?: string;
  branch_id?: string;
  stop_loss_type?: string;
  stop_loss_value?: string;
  square_off_type?: string;
  square_off_value?: string;
  trailing_stop_loss?: string;
  trailing_sl_value?: string;
}

export interface Instrument {
  instrument_token: string;
  exchange_segment: string;
}

export interface QuoteInstrument extends Instrument {}

export type MessageCallback = (msg: any) => void;
export type ErrorCallback = (err: unknown) => void;
export type VoidCallback = (msg?: string) => void;
