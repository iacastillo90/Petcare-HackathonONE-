# Tasks: Redis Caching Implementation

## Phase 1: Infrastructure Setup

- [x] 1.1 Add Redis service to compose.yml (image: redis:7-alpine, port 6379, network: dokploy-network)
- [x] 1.2 Add dependencies to build.gradle: spring-boot-starter-cache, spring-boot-starter-data-redis
- [x] 1.3 Add Redis configuration to application.properties (spring.data.redis.host, spring.data.redis.port, spring.cache.type=redis, spring.cache.redis.time-to-live=43200000)
- [x] 1.4 Create RedisConfig.java in Configurations/ with @EnableCaching and CacheManager (12h TTL, Jackson serializer)

## Phase 2: Core Implementation - User Service

- [x] 2.1 Add @Cacheable(value = "users", key = "#email") to getUserByEmail() in UserServiceImplement
- [x] 2.2 Add @Cacheable(value = "users", key = "#id") to getUserById() in UserServiceImplement
- [x] 2.3 Add @Cacheable(value = "users", key = "'all'") to getAllUsers() in UserServiceImplement
- [x] 2.4 Add @Cacheable(value = "users", key = "#role") to getUsersByRole() in UserServiceImplement
- [x] 2.5 Add @CacheEvict(value = "users", allEntries = true) to createUser() in UserServiceImplement
- [x] 2.6 Add @CacheEvict(value = "users", key = "#id") to updateUser() in UserServiceImplement
- [x] 2.7 Add @CacheEvict(value = "users", key = "#id") to deleteUser() in UserServiceImplement

## Phase 3: Core Implementation - Pet Service

- [x] 3.1 Add @Cacheable(value = "pets", key = "#id") to getPetById() in PetServiceImplement
- [x] 3.2 Add @Cacheable(value = "pets", key = "#accountId") to getPetsByAccountId() in PetServiceImplement
- [x] 3.3 Add @Cacheable(value = "pets", key = "'all'") to getAllPets() in PetServiceImplement
- [x] 3.4 Add @CacheEvict(value = "pets", allEntries = true) to createPet() in PetServiceImplement
- [x] 3.5 Add @CacheEvict(value = "pets", key = "#id") to updatePet() in PetServiceImplement
- [x] 3.6 Add @CacheEvict(value = "pets", key = "#id") to deletePet() in PetServiceImplement

## Phase 4: Core Implementation - Sitter Service

- [x] 4.1 Add @Cacheable(value = "sitters", key = "#userId") to getSitterProfile() in SitterServiceImplement
- [x] 4.2 Add @Cacheable(value = "sitters", key = "'all'") to getAllSitterProfiles() in SitterServiceImplement
- [x] 4.3 Add @Cacheable(value = "sitters", key = "#city") to findSitters() in SitterServiceImplement
- [x] 4.4 Add @CacheEvict(value = "sitters", allEntries = true) to createSitterProfile() in SitterServiceImplement
- [x] 4.5 Add @CacheEvict(value = "sitters", key = "#userId") to updateSitterProfile() in SitterServiceImplement
- [x] 4.6 Add @CacheEvict(value = "sitters", key = "#userId") to deleteSitterProfile() in SitterServiceImplement

## Phase 5: Core Implementation - Service Offering

- [x] 5.1 Add @Cacheable(value = "services", key = "#id") to getServiceById() in ServiceOfferingImplement
- [x] 5.2 Add @Cacheable(value = "services", key = "'all'") to getAllServices() in ServiceOfferingImplement
- [x] 5.3 Add @CacheEvict(value = "services", allEntries = true) to createServiceOffering() in ServiceOfferingImplement
- [x] 5.4 Add @CacheEvict(value = "services", key = "#id") to updateServiceOffering() in ServiceOfferingImplement
- [x] 5.5 Add @CacheEvict(value = "services", key = "#id") to deleteServiceOffering() in ServiceOfferingImplement

## Phase 6: Core Implementation - Booking Service

- [x] 6.1 Add @Cacheable(value = "bookings", key = "#id") to getBookingById() in BookingServiceImplement
- [x] 6.2 Add @Cacheable(value = "bookings", key = "#userId") to getBookingsByUser() in BookingServiceImplement
- [x] 6.3 Add @Cacheable(value = "bookings", key = "'all'") to getAllBookings() in BookingServiceImplement
- [x] 6.4 Add @CacheEvict(value = "bookings", allEntries = true) to createBooking() in BookingServiceImplement
- [x] 6.5 Add @CacheEvict(value = "bookings", key = "#id") to updateBookingStatus() in BookingServiceImplement

## Phase 7: Core Implementation - Other Services

- [x] 7.1 Add @Cacheable to ReviewServiceImplement (getReviewsByPetId, getReviewsByUserId)
- [x] 7.2 Add @CacheEvict to ReviewServiceImplement (createReview)
- [x] 7.3 Add @Cacheable to DiscountCouponImplement (getDiscountCouponByCode)
- [x] 7.4 Add @CacheEvict to DiscountCouponImplement (saveDiscountCoupon)
- [x] 7.5 Add @Cacheable to AccountServiceImplement (getAccountById, getAllAccounts)
- [x] 7.6 Add @CacheEvict to AccountServiceImplement (createAccount, updateAccount)

## Phase 8: Testing & Verification

- [x] 8.1 Run ./gradlew build to verify compilation
- [ ] 8.2 Test locally with docker-compose up (Redis, MySQL, App)
- [ ] 8.3 Verify cache keys created in Redis: redis-cli KEYS "*"
- [ ] 8.4 Test cache hit: call GET /api/users/email-available twice, check logs for cache
- [ ] 8.5 Test cache invalidation: create user, verify users:all evicted
- [x] 8.6 Run existing tests: ./gradlew test
- [ ] 8.7 Verify Swagger works: GET /swagger-ui.html

## Phase 9: Cleanup

- [x] 9.1 Commit all changes to feat/concurrency branch
- [ ] 9.2 Push to remote
- [ ] 9.3 Create PR or deploy to staging
