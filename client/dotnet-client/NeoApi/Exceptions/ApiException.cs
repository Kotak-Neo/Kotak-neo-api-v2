namespace Kotak.Neo.Exceptions;

public class OpenApiException : Exception
{
    public OpenApiException(string message) : base(message) { }
}

public class ApiTypeError : OpenApiException
{
    public ApiTypeError(string msg) : base(msg) { }
}

public class ApiValueError : OpenApiException
{
    public ApiValueError(string msg) : base(msg) { }
}

public class ApiAttributeError : OpenApiException
{
    public ApiAttributeError(string msg) : base(msg) { }
}

public class ApiKeyError : OpenApiException
{
    public ApiKeyError(string msg) : base(msg) { }
}

public class ApiException : OpenApiException
{
    public int Status { get; }
    public string Reason { get; }
    public string? Body { get; }

    public ApiException(int status, string reason, string? body = null)
        : base($"({status}) {reason}{(body is null ? "" : $": {body}")}")
    {
        Status = status;
        Reason = reason;
        Body = body;
    }
}
