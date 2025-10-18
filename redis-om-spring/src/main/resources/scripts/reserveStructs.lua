local keyExists = redis.call('EXISTS', KEYS[1])
if keyExists == 0 then
    redis.call('TOPK.RESERVE', KEYS[1], ARGV[1])
    return true
end
return false