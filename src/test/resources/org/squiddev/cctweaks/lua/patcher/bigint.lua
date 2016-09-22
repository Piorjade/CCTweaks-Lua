local new = biginteger.new

local a = new(123)

assert.assert(a == new(123), "b123 == b123")
assert.assert(a ~= new(124), "b123 ~= b124")
assert.assert(a ~= '123', "b123 ~= '123'")
assert.assert(a ~= 123, "b123 ~= 123")

assert.assertEquals("123", a:tostring(), "b123 == '123'")
assert.assertEquals(123, a:tonumber(), "b123 == 123")

assert.assertEquals(new(124), a + 1, "b123 + 1")
assert.assertEquals(new(124), a + '1', "b123 + '1'")

assert.assertEquals(a, new(-123):abs(), "abs(-123)")
assert.assertEquals(-a, new(-123), "abs(-123)")
