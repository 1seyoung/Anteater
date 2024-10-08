![image](https://github.com/user-attachments/assets/175fb4c6-601d-4380-83fb-ff4bc6fc16e6)

# KOSPI 주식 종목 토론 커뮤니티 프로젝트
프로젝트 기간 : 24/08/11 ~ 진행 중

- 사용자들이 KOSPI 주식 종목에 대한 의견을 나눌 수 있는 가상 주식 커뮤니티의 백엔드 API
- 대규모 사용자 증가나 실시간 데이터 처리와 같은 상황에서도 유연하게 확장할 수 있도록 **Service-Oriented-Architecture**(**SOA**)로 설계하였으며, **도커 컴포즈**를 활용해 여러 서비스를 쉽게 관리하고 배포할 수 있도록 하였습니다.
- Docker 환경을 통해 일관된 개발 및 배포 방식을 적용함으로써, 환경 간 차이를 줄여 안정적인 운영을 추구하고 있습니다.
  
## 유저 플로우
- 유저는 회원 가입을 통해 서비스의 주요 기능을 이용할 수 있습니다.
- 종목에 관한 뉴스피드 화면에서 유저는 포스트를 작성하고, 댓글과 좋아요를 통해 다른 사용자들과 상호작용할 수 있습니다.

## 추후 목표
- [ ] 뉴스피드 서비스에 Kafka 도입하여 이벤트 기반 동작(현재 진행 중 24.09)
- [ ] 프론트엔드를 개발하여 사용자에게 직관적이고 편리한 UI/UX를 제공
- [ ] **Nginx**를 통해 정적 파일을 서빙하고 백엔드 API와의 통신 원활화
- [ ] Airflow와 Grafana 같은 데이터 엔지니어링 플랫폼을 적용해 뉴스피드 상호작용 데이터를 분석
- [ ] 사람들이 많이 관심을 가지는 종목을 시각적으로 확인할 수 있는 대시보드 개발
- [ ] 한국투자증권의 Open API를 활용하여 실시간 주식 데이터를 웹소켓으로 제공

## Architecture

![image](https://github.com/user-attachments/assets/66151d71-499d-4d12-b1ca-d02a944c4b68)


## Tech

|  | Tech           | Usage                                           |
|--|----------------|-------------------------------------------------|
|**Language** | Java 17   |                                          |
|**Framework**|SpringBoot 3.3.3 |                    |
|**Build**| Gradle     | 빌드툴                                    |
|**Database** | MySQL   | 관계형 데이터베이스                         |
|**Database** | Redis | |
|**ORM**|Spring Data JPA         |                                  |
|**VCS**| Git  | 버전관리            |
 | **Message Broker** | Kafka | 서비스 간 데이터 통신 (이벤트 기반) |
| **API Gateway** | Spring Cloud Gateway | API 라우팅 및 필터링 |
|**Container** | Docker | WAS 및 마이크로서비스에 필요한 의존성 컨테이너 구동 |
| **Service Discovery** | Spring Cloud Netflix Eureka | 서비스 디스커버리 및 로드 밸런싱 |


## 주요 기능 및 API
| 마이크로서비스            | 기능                                                             |
|--------------------------|----------------------------------------------------------------|
| **API Gateway Service**   | API 단일 진입점, 라우팅 기능, 토큰 검증                                                |
| **Registry Service**      | 서비스 디스커버리 및 로드밸런싱 기능 제공                                            |
| **User Service**          | 회원 가입, 개인정보 업데이트, 로그인, 로그아웃 API 제공                                |
| **Activity Service**      | 포스트 작성, 댓글 작성, 좋아요 클릭 API 제공                                           |
| **Newsfeed Service**      | 활동 데이터를 카프카로 전송하여 뉴스피드 아이템 구성                                     |
| **Stock Service**         | 관심 종목 팔로우, 종목 정보 관리 API 제공                                           |

## ERD 
![image](https://github.com/user-attachments/assets/af574540-8856-4e3d-8b5a-a7e967561cb8)

## 트러블 슈팅
**인증/인가 트러블 슈팅 관련** : [멤버 서비스 시퀀스 다이어그램 바로 보기](https://github.com/1seyoung/Anteater/blob/main/member-service/README_MEMBER_SERVICE.md)
<H3>인증/인가(1) : Member Service: 회원 관리와 인증 기능 통합 사례</H3>

****문제 정의****

**문제:**
회원 관리(회원 가입, 개인정보 수정 등)와 인증/인가(토큰 발급 및 검증) 기능을 어떻게 효과적으로 결합할 것인지 고민이 있었음. 두 기능이 밀접하게 관련되어 있어 별도 서비스로 분리하는 대신, 하나의 Member Service에 통합하여 관리하기로 결정했으나, 이를 통해 생길 수 있는 확장성 및 유지보수 문제를 고려해야 했음.

****원인 분석****

- 유저 관련 기능과 인증/인가 기능이 밀접하게 연결되어 있으므로, 두 기능을 별도로 분리하기보다는 하나의 서비스로 통합하는 것이 더 효율적일 수 있다고 판단.
- 별도의 인증 서비스를 두는 경우, 서비스 간 통신 및 데이터 동기화 문제가 발생할 수 있다고 생각하여 Member Service에 모든 기능을 통합.
- 그러나 통합된 서비스의 규모가 커질 경우, 서비스 복잡성이 증가하고 유지보수가 어려워질 가능성을 우려.

****해결 방법****

**Member Service 통합 설계:**
- 회원 관리와 인증/인가 기능을 Member Service로 통합하여 관리. 이를 통해 서비스 간 불필요한 통신을 최소화하고, 데이터 일관성을 쉽게 유지할 수 있도록 함.
- 회원 가입, 개인정보 수정, 로그인, 로그아웃과 같은 기능뿐 아니라, 토큰 발급 및 검증 등의 인증 기능도 하나의 서비스에서 처리.

**API Gateway에서의 인가 체크:**
- API Gateway에서 각 요청이 Member Service로 전달되기 전에 JWT 토큰 검증을 통해 인가(Authorization)를 처리하도록 설계.
- 게이트웨이에서 인가 필터를 추가하여, 유효한 토큰을 확인한 후 요청을 Member Service로 전달.
- 이를 통해 서비스 통합은 이루었지만, 인가는 중앙 집중화된 게이트웨이에서 효율적으로 관리.

**서비스 복잡성 관리:**
- Member Service의 복잡성이 증가하지 않도록, 모듈화된 코드 구조를 적용.
- 인증 관련 로직은 독립된 모듈로 관리하여, 코드가 섞이지 않도록 구조화함.
- 추후 필요시 인증 관련 기능을 독립된 서비스로 분리할 수 있도록 코드 및 아키텍처를 유연하게 설계.

****결과****

- 유저와 인증 관련 로직을 Member Service로 통합하여, 데이터 동기화 문제나 서비스 간 통신 오버헤드를 줄일 수 있었음.
- API Gateway를 통해 인증 및 인가를 중앙에서 관리하면서, 각 서비스는 불필요한 인증 로직을 따로 처리할 필요가 없어짐.
- 통합된 구조 덕분에 코드 관리가 수월해졌으며, 서비스 간의 의존성도 감소.

****교훈 및 개선 사항****

- 유저와 인증 기능의 통합은 복잡성을 줄이고 데이터 일관성을 쉽게 유지하는 장점이 있었음.
- 그러나 서비스가 커질수록, 이를 분리하는 것이 필요할 수도 있으므로 코드 모듈화와 아키텍처 확장 가능성을 염두에 두어야 함.
- 추후 OAuth2와 같은 표준 인증 방식을 도입하거나, 인증 기능을 독립 서비스로 분리할 필요성이 있을 수 있음.

<H3>인증/인가(2) : API Gateway와 서비스 간 인증/인가 역할 분담</H3>

**문제 정의**

**문제:**
API Gateway가 Access Token을 검증하고 인가(Authorization)의 역할까지 수행하도록 설정하였음. 그러나 API Gateway가 인가를 처리하는 것이 최선인지, 그리고 이 방식이 적절한 확장성과 보안성을 제공하는지에 대한 고민이 있었음.

**원인 분석**

- API Gateway는 서비스 간의 중앙 진입점으로서, 모든 요청에 대해 Access Token을 검증하여 유효성을 확인할 수 있음. 이를 통해 각 서비스에서 별도로 토큰 검증을 하지 않아도 됨.
- 그러나, API Gateway에서 인가까지 처리하는 것이 옳은지에 대한 고민이 있었음. **인가(Authorization)**는 서비스별로 세부 권한 관리가 필요할 수 있기 때문.
- 인가를 API Gateway에서 처리할 경우, 서비스별로 다르게 설정된 권한을 효과적으로 관리할 수 있는지가 주요 이슈였음.

**해결 방법**

1. **API Gateway에서 Access Token 검증:**
   - API Gateway는 요청이 각 서비스로 전달되기 전에 JWT Access Token의 유효성을 검증.
   - 이를 통해 기본적인 인증(Authentication) 역할을 처리하고, 각 서비스에서는 추가적인 인증 로직을 처리하지 않도록 설정.

2. **세부 권한 관리(Authorization) 분리:**
   - Access Token의 검증은 API Gateway에서 처리하되, 세부적인 권한 관리는 각 서비스 내에서 처리하도록 설계.
   - 예를 들어, 유저 서비스나 주식 서비스에서는 API Gateway가 유효한 토큰을 검증한 후, 해당 유저가 특정 자원에 접근할 권한이 있는지 추가적인 검증을 서비스 내에서 처리하도록 구현.

3. **API Gateway의 역할 최적화:**
   - API Gateway는 단순히 토큰의 유효성 검증과 라우팅에 집중하고, 복잡한 권한 관리는 각 서비스에서 처리하도록 함.
   - 이를 통해 API Gateway의 책임을 제한하고, 권한 관련 로직은 각 서비스의 비즈니스 로직에 포함하여 유연성을 확보.

**결과**

- API Gateway가 Access Token 검증만을 담당하고, 각 서비스는 세부적인 권한 관리를 처리함으로써 역할의 명확한 분리가 이루어짐.
- 유효성 검증을 API Gateway에서 처리하므로, 각 서비스는 토큰 검증 로직을 신경 쓸 필요가 없어져 코드가 간결해짐.
- 인가 로직을 각 서비스에 두어, 서비스별로 다르게 정의된 권한 정책을 유연하게 관리할 수 있었음.

**교훈 및 개선 사항**

- **인증(Authentication)**과 **인가(Authorization)**를 명확히 구분하여 처리하는 것이 중요함을 다시 한 번 깨달음.
- API Gateway는 단순히 Access Token 검증과 요청 라우팅에 집중해야 하며, 세부적인 권한 관리는 각 서비스에서 처리하는 것이 더 유연하고 확장성이 좋다는 결론을 내림.



## 기술적 의사결정

<H3>Kafka 도입에 대한 결정</H3>

**1. 결정 배경**

가상 주식 커뮤니티 프로젝트에서 도커로 배포된 여러 서비스 간에 데이터를 교환할 수 있는 방법을 검토하였다. 서비스 간의 효율적인 데이터 흐름과 시스템 확장성을 고려하여, 정보 교환을 위한 적합한 기술을 선택할 필요가 있었다.

**2. 결정 목표**

- 서비스들이 독립적으로 동작하면서, 안정적으로 데이터를 주고받을 수 있는 이벤트 기반 아키텍처를 구현하는 것이 목표다.
- 실시간 데이터 스트리밍 처리 요구사항을 충족할 수 있는 성능과 확장성을 가진 기술을 선택하고자 했다.
- 서비스 간 결합도를 낮추어 유연한 시스템 변경 및 확장이 가능하도록 설계하는 것이 중요했다.

**3. 고려된 기술 옵션**

1. **Kafka**
   - 고성능 이벤트 스트리밍 플랫폼으로, 대규모 데이터를 실시간으로 처리할 수 있다.
   - 이벤트 기반 아키텍처를 쉽게 구현할 수 있는 장점이 있다.

2. **RabbitMQ**
   - 메시지 큐 시스템으로, 서비스 간 데이터를 큐 방식으로 전송하여 처리한다.
   - 상대적으로 가볍고 단순한 메시지 전달에 강점이 있다.

**4. 의사결정 이유**

추후 프로젝트 확장을 고려했을 때, 서비스 간 결합도를 낮추고 유연한 확장이 가능한 아키텍처가 필요했다. 특히, 실시간 데이터를 효율적으로 처리할 수 있는 시스템이 중요했고, 다양한 이벤트를 쉽게 관리할 수 있는 구조를 원했다. 이러한 요구 사항을 만족하기 위해 Kafka를 선택하게 되었다. 그 이유는 다음과 같다

1. **이벤트 기반 아키텍처**
   - Kafka는 이벤트 기반 아키텍처를 지원하여, 서비스 간 결합도를 줄이고, 비동기 방식으로 유연하게 확장할 수 있는 구조를 제공한다. 또한, 모든 소프트웨어 동작을 이벤트로 표현하는 이산사건 시스템 모델과도 잘 맞아떨어지며, 발생하는 이벤트를 기록하고 처리하는 데 적합하다.

2. **실시간 데이터 처리**
   - Kafka는 대용량의 데이터를 실시간으로 처리하는 데 최적화되어 있다. 가상 주식 커뮤니티에서는 뉴스피드의 사용자 상호작용 데이터 분석 및 주식 관련 실시간 데이터 처리가 필요한데, Kafka는 이러한 요구사항을 충족할 수 있다.

3. **데이터 보존 및 확장성**
   - RabbitMQ는 메시지를 처리하고 나면 즉시 소비되고 사라지는 특성이 있지만, Kafka는 데이터를 로그로 저장해두고, 필요한 경우 나중에 다시 소비할 수 있다. 이는 뉴스피드와 같은 시스템에서 과거 데이터를 분석하거나 다시 사용할 때 유리하다. 또한, Kafka는 대규모 시스템에서도 높은 확장성을 제공하여, 데이터 손실 없이 안정적으로 대용량 메시지를 처리할 수 있어 장기적인 확장성과 유지보수에 적합하다.
**5. 결론**

Kafka는 가상 주식 커뮤니티 프로젝트에서 서비스 간의 데이터 교환을 처리하고, 추후 실시간 데이터 스트리밍을 원활하게 지원하기 위한 최적의 선택이다. 이벤트 기반 아키텍처와 실시간 데이터 처리가 필요한 프로젝트 요구사항에 가장 적합한 기술로 선택하였다.


