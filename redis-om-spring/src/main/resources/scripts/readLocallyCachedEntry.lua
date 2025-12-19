-- Reads a cache entry that is resident within the memory of the calling application
-- params KEYS[1] - key being interrogated
-- params ARGV[1] - the lastModifiedTime according to the client
-- params ARGV[2] - the new lastAccessedTime to write
-- return [false, nil] if cacheEntry is not present
-- return [false, array] if cacheEntry is present, but has been modified since the provided last modified lastModifiedTime
-- return [true, nil] if cacheEntry is present and has not been modified since the provided lastModifiedTime

local lastModifiedTimeStr = redis.call('HGET', KEYS[1], 'lastModifiedTime')

if lastModifiedTimeStr == false then
    return {false, nil}
end

local lastModifiedTime = tonumber(lastModifiedTimeStr)

redis.call('HSET', KEYS[1], "lastAccessedTime", ARGV[2])

if lastModifiedTime > tonumber(ARGV[1]) then
    local body = redis.call("HGETALL", KEYS[1], '$')
    return {false, body}
end

return {true, nil}