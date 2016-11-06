os.setComputerLabel(("x"):rep(100))
assert.assertEquals(("x"):rep(32), os.getComputerLabel(), "Limits length")

os.setComputerLabel(("\0 1 \255"))
assert.assertEquals(" 1 ", os.getComputerLabel(), "Removes non-printables")
