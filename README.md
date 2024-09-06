# Anteater
KOSPI 주식 종목에 대한 의견을 나누는 가상 주식 커뮤니티의 백엔드 API 입니다.
서비스간의 독립적인 개발을 위하여 Service Orient Architecture 로 설계했습니다.

## 유저 플로우
- 유저는 회원 가입을 통해 서비스의 주 기능을 이용합니다.

## Tech

|  | Tech           | Usage                                           |
|--|----------------|-------------------------------------------------|
|**Language** | Java 17   |                                          |
|**Framework**|SpringBoot 3.3.3 |                    |
|**Build**| Gradle     | 빌드툴                                    |
|**Database** | MySQL   | 관계형 데이터베이스                         |
|**ORM**|Spring Data JPA         |                                  |
|**VCS**| Git  | 버전관리            |



## 주요 기능 및 API
| Service | Function|
|API Gateway |API 단일 진입점, 라우팅 기능|
|Eureka Server |
