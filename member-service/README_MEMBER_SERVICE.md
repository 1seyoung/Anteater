
### 로그인 시퀀스 다이어그램

```mermaid
sequenceDiagram
    participant Client
    participant API_Gateway
    participant Member_Service
    participant MySQL
    participant Redis

    Client->>API_Gateway: 1. /login 요청 (ID, PWD)
    API_Gateway->>Member_Service: 2. 인증요청 및 검증 요청
    Member_Service->>MySQL: 3. authenticate()
    MySQL-->>Member_Service: 4. 인증 결과
    
    alt 유효한 정보
        Member_Service->>Redis: 5. Save Refresh Token
        Member_Service-->>Client: return ok 반환<br/>Access Token 전달
    else 유효하지 않은 정보
        Member_Service-->>Client: Invalid Info
    end
    
    opt 런타임 예외 발생
        Member_Service-->>Client: Runtime Exception
    end
