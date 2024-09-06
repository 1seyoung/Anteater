# KOSPI 주식 종목 의견 공유 가상 커뮤니티 백엔드 API : Anteater                                                                                                                                    

프로젝트 기간 : 24/08/11 ~ 진행 중

- 사용자들이 KOSPI 주식 종목에 대한 의견을 나눌 수 있는 가상 주식 커뮤니티의 백엔드 API입니다.
- 대규모 사용자 증가나 실시간 데이터 처리와 같은 상황에서도 유연하게 확장할 수 있도록 **Service-Oriented Architecture(SOA)**로 설계하였으며, **도커 컴포즈**를 활용해 여러 서비스를 쉽게 관리하고 배포할 수 있도록 하였습니다.
- 실제 KOSPI 데이터를 기반으로 API 테스트를 진행하고 있으며, Docker 환경에서의 일관된 개발 및 배포로 신뢰성과 확장성을 보장하고 있습니다.
  
## 유저 플로우
- 유저는 회원 가입을 통해 서비스의 주요 기능을 이용할 수 있습니다.
- 종목에 관한 뉴스피드 화면에서 유저는 포스트를 작성하고, 댓글과 좋아요를 통해 다른 사용자들과 상호작용할 수 있습니다.

## 추후 목표
- [ ] 프론트엔드를 개발하여 사용자에게 직관적이고 편리한 UI/UX를 제공
- [ ] **Nginx**를 통해 정적 파일을 서빙하고 백엔드 API와의 통신 원활화
- [ ] Airflow와 Grafana 같은 데이터 엔지니어링 플랫폼을 적용해 뉴스피드 상호작용 데이터를 분석
- [ ] 사람들이 많이 관심을 가지는 종목을 시각적으로 확인할 수 있는 대시보드 개발
- [ ] 한국투자증권의 Open API를 활용하여 실시간 주식 데이터를 웹소켓으로 제공

## Architecture



## Tech

|  | Tech           | Usage                                           |
|--|----------------|-------------------------------------------------|
|**Language** | Java 17   |                                          |
|**Framework**|SpringBoot 3.3.3 |                    |
|**Build**| Gradle     | 빌드툴                                    |
|**Database** | MySQL   | 관계형 데이터베이스                         |
|**ORM**|Spring Data JPA         |                                  |
|**VCS**| Git  | 버전관리            |
 | **Message Broker** | Kafka | 서비스 간 데이터 통신 (이벤트 기반) |
| **API Gateway** | Spring Cloud Gateway | API 라우팅 및 필터링 |
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

