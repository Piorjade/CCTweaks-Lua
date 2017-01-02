local function randomBytes()
	local out = {}
	for i = 1, 1024 do
		out[i] = string.char(math.random(0, 255))
	end

	return table.concat(out)
end

for i = 1, 10 do
	local str = randomBytes()
	os.queueEvent('bin_event', str)
	local _, msg = os.pullEvent('bin_event')
	assert.assertEquals(str, msg)
end
