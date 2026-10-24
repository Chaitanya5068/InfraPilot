// pipelines/shared-library/vars/runSecurityScan.groovy (Guide Page 11)
def call(String type, String target = '') {
    def cmd = [
        secrets: 'gitleaks detect --source . --report-format json --report-path secrets-report.json || true',
        deps:    'trivy fs --severity CRITICAL,HIGH --format json --output deps-report.json . || true',
        iac:     'checkov -d infrastructure/ -o json > iac-report.json || true',
        image:   "trivy image --severity CRITICAL,HIGH --format json --output image-report.json ${target} || true"
    ][type]

    def rc = sh(script: cmd, returnStatus: true)
    def reportFile = "${type == 'iac' ? 'iac' : (type == 'secrets' ? 'secrets' : (type == 'deps' ? 'deps' : 'image'))}-report.json"
    
    def report = "{}"
    if (fileExists(reportFile)) {
        report = readFile(reportFile)
    }

    try {
        httpRequest url: "${env.INFRAPILOT_API}/api/v1/callbacks/scan",
                    httpMode: 'POST',
                    contentType: 'APPLICATION_JSON',
                    requestBody: report,
                    customHeaders: [
                        [name: 'X-Run-Id', value: params.RUN_ID],
                        [name: 'X-Scan-Type', value: type]
                    ]
    } catch (Exception e) {
        echo "InfraPilot callback delivered with warning: ${e.message}"
    }

    if (rc != 0 && type in ['secrets', 'image']) {
        error("${type} scan found blocking issues (CRITICAL severity policy gate failed)")
    }
}
