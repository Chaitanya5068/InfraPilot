// pipelines/shared-library/vars/runTests.groovy
def call(String testType, String testCommand) {
    echo "Executing ${testType} test suite: ${testCommand}"
    sh testCommand
}
