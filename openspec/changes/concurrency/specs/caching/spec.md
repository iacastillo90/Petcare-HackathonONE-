# Delta for Caching

## Purpose
This spec defines requirements for implementing Redis caching layer in the Petcare application to reduce database load and improve response times for frequent read operations.

## ADDED Requirements

### Requirement: Redis Service Availability
The system MUST have Redis 7.x available as a caching layer.

#### Scenario: Redis is available
- GIVEN the application is starting
- WHEN it attempts to connect to Redis
- THEN it SHOULD establish a connection and be able to use caching

#### Scenario: Redis is unavailable
- GIVEN Redis service is down or unreachable
- WHEN the application attempts to cache data
- THEN it SHOULD gracefully degrade (no cache, direct DB queries)

### Requirement: Cache Read Operations
The system SHALL cache read-heavy operations for 12 hours.

#### Scenario: Cache hit for user by email
- GIVEN a user exists in cache with key "users:email:test@example.com"
- WHEN getUserByEmail("test@example.com") is called
- THEN return cached user without querying database

#### Scenario: Cache miss for user
- GIVEN no cached entry exists for "users:email:test@example.com"
- WHEN getUserByEmail("test@example.com") is called
- THEN query database, store result in cache, and return user
- AND subsequent calls within 12h return cached result

#### Scenario: Cache hit for all services
- GIVEN getAllServices() was called within last 12 hours
- WHEN getAllServices() is called again
- THEN return cached list of services without DB query

### Requirement: Cache Invalidation on Write
The system MUST evict relevant cache entries when data is modified.

#### Scenario: Create new user
- GIVEN a new user is created via createUser()
- WHEN the user is successfully saved to database
- THEN invalidate all "users" cache entries
- AND subsequent getAllUsers() queries database

#### Scenario: Update existing user
- GIVEN a user with id=5 exists in cache
- WHEN updateUser(5, ...) is called
- THEN invalidate cache entry with key "users:5"
- AND subsequent getUserById(5) returns fresh data

#### Scenario: Delete user
- GIVEN a user with id=5 exists in cache
- WHEN deleteUser(5) is called
- THEN remove cache entry "users:5"
- AND remove cache entry "users:email:..."

#### Scenario: Update service offering
- GIVEN a service with id=10 exists in cache
- WHEN updateServiceOffering(10, ...) is called
- THEN invalidate "services:10" cache entry
- AND invalidate "services:all" cache entry

### Requirement: Cache TTL
Cached entries MUST expire after 12 hours.

#### Scenario: Cache entry expires
- GIVEN a cache entry was created 12 hours ago
- WHEN any operation accesses that cache key
- THEN the entry is not found (cache miss)
- AND the system queries the database for fresh data

### Requirement: Cache Key Structure
Cache keys MUST follow a consistent naming pattern.

#### Scenario: Cache key format
- GIVEN user with id=5 and email="test@example.com"
- WHEN caching user data
- THEN use keys "users:5" and "users:email:test@example.com"

#### Scenario: Cache key for lists
- GIVEN requesting all users
- WHEN caching list result
- THEN use key "users:all"

## MODIFIED Requirements
(None - this is a new feature)

## REMOVED Requirements
(None)

### Cache Names per Entity
- users - User entities and lists
- pets - Pet entities and lists
- sitters - SitterProfile entities and lists
- services - ServiceOffering entities and lists
- bookings - Booking entities and lists
- reviews - Review entities and lists
- invoices - Invoice entities and lists
- discounts - DiscountCoupon entities
