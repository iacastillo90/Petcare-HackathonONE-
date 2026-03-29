# Design: Redis Caching Implementation

## Technical Approach
Use Spring Boot's caching abstraction with Redis as the backing store. This provides:
- Declarative caching via annotations (@Cacheable, @CacheEvict)
- Automatic cache key generation
- TTL configuration at cache manager level
- Graceful degradation when Redis is unavailable

## Architecture Decisions

### Decision: Spring Cache vs Manual Redis
**Choice**: Spring Cache abstraction with @Cacheable/@CacheEvict
**Alternatives considered**: Manual RedisTemplate operations (cache-aside pattern)
**Rationale**: Cleaner code, less boilerplate, automatic key generation, easy invalidation

### Decision: Jackson vs Kryo Serializer
**Choice**: Jackson JSON serialization
**Alternatives considered**: Kryo, Java serialization
**Rationale**: Better compatibility with existing JSON infrastructure, readable cache entries for debugging

### Decision: TTL per Cache vs Global
**Choice**: Global 12h TTL configured in CacheManager
**Alternatives considered**: Per-method TTL via annotation
**Rationale**: Simpler configuration, consistent expiration policy across all caches

### Decision: Cache Key Strategy
**Choice**: Spring EL expressions for keys
**Alternatives considered**: Custom key generators
**Rationale**: Sufficient for current use cases, less complexity

## Data Flow

### Read Operation (Cached)
```
Request → Service.getUserById(5)
           ↓
         @Cacheable("users") → Check Redis for key "users:5"
           ↓ (cache miss)
         Repository.findById(5) → Database
           ↓
         Store in Redis "users:5" with 12h TTL
           ↓
         Return User
```

### Write Operation (Cache Eviction)
```
Request → Service.updateUser(5, data)
           ↓
         Repository.save(user) → Database
           ↓
         @CacheEvict(value="users", key="#id") → Delete Redis key "users:5"
           ↓
         Return updated User
```

## File Changes

| File | Action | Description |
|------|--------|-------------|
| compose.yml | Modify | Add Redis service (redis:7-alpine, port 6379) |
| build.gradle | Modify | Add spring-boot-starter-cache, spring-boot-starter-data-redis |
| application.properties | Modify | Add Redis configuration (host, port) |
| RedisConfig.java | Create | CacheManager with 12h TTL, RedisTemplate with Jackson |
| UserServiceImplement.java | Modify | Add @Cacheable to reads, @CacheEvict to writes |
| PetServiceImplement.java | Modify | Add @Cacheable to reads, @CacheEvict to writes |
| SitterServiceImplement.java | Modify | Add @Cacheable to reads, @CacheEvict to writes |
| ServiceOfferingImplement.java | Modify | Add @Cacheable to reads, @CacheEvict to writes |
| BookingServiceImplement.java | Modify | Add @Cacheable to reads, @CacheEvict to writes |
| ReviewServiceImplement.java | Modify | Add @Cacheable to reads, @CacheEvict to writes |
| DiscountCouponImplement.java | Modify | Add @Cacheable to reads, @CacheEvict to writes |
| AccountServiceImplement.java | Modify | Add @Cacheable to reads, @CacheEvict to writes |

## Cache Configuration

### application.properties additions
```properties
# Redis
spring.data.redis.host=${REDIS_HOST:redis}
spring.data.redis.port=${REDIS_PORT:6379}

# Cache
spring.cache.type=redis
spring.cache.redis.time-to-live=43200000
spring.cache.redis.cache-null-values=false
```

### RedisConfig.java structure
```java
@Configuration
@EnableCaching
public class RedisConfig {
    
    @Bean
    public CacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
            .entryTtl(Duration.ofHours(12))
            .serializeValuesWith(
                RedisSerializationContext.SerializationPair.fromSerializer(
                    new GenericJackson2JsonRedisSerializer()
                )
            );
        
        return RedisCacheManager.builder(connectionFactory)
            .cacheDefaults(config)
            .build();
    }
}
```

## Service Annotations Pattern

### Read operations (@Cacheable)
```java
@Cacheable(value = "users", key = "#email", unless = "#result == null")
public UserResponse getUserByEmail(String email) { ... }

@Cacheable(value = "users", key = "#id")
public UserResponse getUserById(Long id) { ... }

@Cacheable(value = "users", key = "'all'")
public List<UserResponse> getAllUsers() { ... }
```

### Write operations (@CacheEvict)
```java
@CacheEvict(value = "users", allEntries = true)
public UserResponse createUser(...) { ... }

@CacheEvict(value = "users", key = "#id")
public UserResponse updateUser(...) { ... }

@CacheEvict(value = "users", key = "#id")
public void deleteUser(...) { ... }
```

## Testing Strategy

| Layer | What to Test | Approach |
|-------|-------------|----------|
| Unit | Cache annotations applied correctly | Mock Redis, verify @Cacheable methods return cached |
| Integration | Full cache lifecycle | Test cache hit/miss, TTL expiration, eviction |
| Manual | Redis CLI to inspect keys | redis-cli KEYS "users:*" |

## Migration / Rollout

1. Add Redis to compose.yml
2. Add dependencies to build.gradle
3. Create RedisConfig.java
4. Add annotations to services one by one
5. Test locally with docker-compose up
6. Deploy to staging, verify cache keys created
7. Deploy to production

No data migration required - cache is populated on first read.

## Open Questions
- [ ] None - design is complete
