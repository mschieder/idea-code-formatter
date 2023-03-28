log = new File(basedir, 'build.log')
test1 = new File(basedir, 'src/main/java/Test1.java')
test1Expected = new File(basedir, 'expected/src/main/java/Test1.java')
assert log.exists()
assert log.text.contains('src/main/java/Test1.java...OK')
assert log.text.contains('1 file(s) scanned.')
assert log.text.contains('1 file(s) formatted.')

assert test1.text == test1Expected.text
return true