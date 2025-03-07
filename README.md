#  Bifröst API Gateway

**Bifröst** is a JAX-RS-based API Gateway with **rate limiting, circuit breaking, and dynamic configuration**. It acts as a central entry point for routing API requests to backend microservices while ensuring **fault tolerance** and **security**.

---

##  Features

 **JWT Authentication** - Secures API requests with Bearer Tokens  
 **Rate Limiting** - Prevents API abuse using Redis-based request limits  
 **Circuit Breaker (Resilience4j)** - Prevents failures from cascading across microservices  
 **Dynamic Routing** - Routes are configured via `config.json` instead of being hardcoded  
 **Exception Handling** - Uses `ExceptionMapper` for structured error responses  
 **Logging & Monitoring** - Logs circuit breaker state changes and request failures

---

## ⚙️ Configuration

###  `config.json` (Dynamic API & Circuit Breaker Config)

Example **config.json**:

```json
{
  "routes": {
    "ordersService": "http://localhost:5001",
    "usersService": "http://localhost:5002",
    "paymentsService": "http://localhost:5003"
  },
  "circuitBreakers": {
    "ordersService": {
      "failureRateThreshold": 50,
      "waitDurationInOpenState": 10,
      "slidingWindowSize": 10,
      "minimumNumberOfCalls": 5
    }
  }
}
