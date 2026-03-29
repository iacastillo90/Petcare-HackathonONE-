# Proposal: Redis Caching for Performance Optimization

## Intent
Implement Redis caching layer to reduce database load and improve response times for frequent read operations. Currently every request hits MySQL directly, causing unnecessary load for data that rarely changes (catalogs, user profiles, service offerings).

## Scope

### In Scope
1. Add Redis service to compose.yml
2. Configure Spring Cache with Redis (spring-boot-starter-data-redis, spring-boot-starter-cache)
3. Configure RedisConnectionFactory and CacheManager with 12h TTL
4. Add @Cacheable annotations to read-heavy service methods
5. Add @CacheEvict annotations to write operations
6. Configure Redis serializer (Jackson or Kryo)
7. Add Redis configuration to application.properties

### Out of Scope
- Distributed locking (future enhancement)
- Cache-aside pattern with manual cache management
- Multi-node cache synchronization

## Approach
Use Spring Cache abstraction with Redis as backing store:
- @Cacheable for GET operations (cached for 12 hours)
- @CacheEvict for CREATE/UPDATE/DELETE operations
- TTL configured at 12 hours (43200000 ms) in CacheManager
- Separate cache names per entity: users, pets, sitters, services, bookings, reviews, invoices, discounts

## Affected Areas

| Area | Impact | Description |
|------|--------|-------------|
| compose.yml | Modified | Add Redis service |
| build.gradle | Modified | Add Redis and cache dependencies |
| application.properties | Modified | Redis connection config |
| RedisConfig.java | New | CacheManager and RedisTemplate configuration |
| UserServiceImplement | Modified | Add @Cacheable and @CacheEvict |
| PetServiceImplement | Modified | Add @Cacheable and @CacheEvict |
| SitterServiceImplement | Modified | Add @Cacheable and @CacheEvict |
| ServiceOfferingImplement | Modified | Add @Cacheable and @CacheEvict |
| BookingServiceImplement | Modified | Add @Cacheable and @CacheEvict |
| ReviewServiceImplement | Modified | Add @Cacheable and @CacheEvict |
| DiscountCouponImplement | Modified | Add @Cacheable and @CacheEvict |
| AccountServiceImplement | Modified | Add @Cacheable and @CacheEvict |

## Risks

| Risk | Likelihood | Mitigation |
|------|------------|------------|
| Cache invalidation missed | Medium | Comprehensive audit of all write methods |
| Stale data (12h TTL) | Low | Acceptable for catalog data; manual flush endpoint |
| Redis connection failure | Low | Fallback to no-cache mode with graceful degradation |
| Serialization issues | Medium | Use Jackson JSON serialization with type info |

## Rollback Plan
1. Remove Redis from compose.yml
2. Remove @Cacheable/@CacheEvict annotations from services
3. Remove Redis dependencies from build.gradle
4. Remove Redis config from application.properties
5. Delete RedisConfig.java

## Dependencies
- Redis 7.x (docker image: redis:7-alpine)
- spring-boot-starter-cache
- spring-boot-starter-data-redis

## Success Criteria
- [ ] Redis service running in Docker
- [ ] All identified read methods cached with 12h TTL
- [ ] All write methods properly evict caches
- [ ] GET /v3/api-docs works (swagger)
- [ ] Performance improvement measurable (>50% reduction in DB queries for cached endpoints)
- [ ] No data inconsistency bugs from stale cache
