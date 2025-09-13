#!lua name=redisSessions

local function touch_key(keys, args)
    -- check if an expiration was passed into the function.
    if args[1] then
        redis.call('EXPIRE', keys[1], args[1])
    end

    local size = redis.call('MEMORY', 'USAGE', keys[1], 'SAMPLES', 0)
    redis.call('HSET', keys[1], 'sessionSize', size)
    return size
end

local function read_key(keys, args)
    local str = redis.call('HGET', keys[1], 'maxInactiveInterval')

    if str == false then
        return nil
    end

    local expiry = tonumber(str)
    redis.call('EXPIRE', keys[1], expiry)
    return redis.call('HGETALL', keys[1])

end

local function read_locally_cached_entry(keys, args)
    local lastModifiedTimeStr = redis.call('HGET', keys[1], 'lastModifiedTime')

    if lastModifiedTimeStr == false then
        return {false, nil}
    end

    local lastModifiedTime = tonumber(lastModifiedTimeStr)

    redis.call('HSET', keys[1], "lastAccessedTime", args[2])

    if lastModifiedTime > tonumber(args[1]) then
        local body = redis.call("HGETALL", keys[1])
        return {false, body}
    end

    return {true, nil}
end

local function reserve_structs(keys, args)
    local keyExists = redis.call('EXISTS', keys[1])
    if keyExists == 0 then
        redis.call('TOPK.RESERVE', keys[1], args[1])
        return true
    end
    return false
end

redis.register_function('touch_key', touch_key)
redis.register_function('read_key', read_key)
redis.register_function('read_locally_cached_entry', read_locally_cached_entry)
redis.register_function('reserve_structs', reserve_structs)