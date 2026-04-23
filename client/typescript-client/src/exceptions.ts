export class OpenApiError extends Error {
  constructor(message: string) {
    super(message);
    this.name = this.constructor.name;
  }
}

export class ApiTypeError extends OpenApiError {}
export class ApiValueError extends OpenApiError {}
export class ApiAttributeError extends OpenApiError {}
export class ApiKeyError extends OpenApiError {}

export class ApiException extends OpenApiError {
  status: number;
  reason: string;
  body: string | undefined;

  constructor(status: number, reason: string, body?: string) {
    super(`(${status}) ${reason}${body ? `: ${body}` : ""}`);
    this.status = status;
    this.reason = reason;
    this.body = body;
  }
}
