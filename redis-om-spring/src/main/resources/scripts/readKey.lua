local str = redis.call('HGET', KEYS[1], 'maxInactiveInterval')

if str == false then
    return nil
end

local expiry = tonumber(str)
redis.call('EXPIRE', KEYS[1], expiry)
return redis.call('HGETALL', KEYS[1])
