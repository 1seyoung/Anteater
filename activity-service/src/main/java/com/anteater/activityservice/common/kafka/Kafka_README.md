## Config

KafkaConfig.java

```text
Kafka 관련 설정을 담당
KafkaTemplate, ProducerFactory, ConsumerFactory 등을 설정
```

## Producer
    
```text
이벤트를 Kafka에 발행하는 메서드를 포함
```

## Consumer

```text
Kafka에서 이벤트를 소비하는 메서드를 포함
여러 토픽을 구독하고 이벤트 타입에 따라 처리

```

## Dto

```text
BaseEvent.java: 모든 이벤트의 기본이 되는 추상 클래스
구체적인 이벤트 클래스들
    - PostCreatedEvent
    - PostUpdatedEvent
    - PostDeletedEvent
```

## Serializer

```text
EventSerializer.java: 이벤트 객체를 JSON으로 직렬화
EventDeserializer.java: JSON을 이벤트 객체로 역직렬화
```