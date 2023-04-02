log = new File(basedir, 'build.log')
assert log.exists()
assert log.text.contains('src/main/java/Test1.java...Needs reformatting')
assert log.text.contains('1 file(s) scanned.')
assert log.text.contains('some files are not clean')
return true