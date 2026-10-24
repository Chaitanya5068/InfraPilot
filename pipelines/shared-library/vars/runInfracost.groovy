// pipelines/shared-library/vars/runInfracost.groovy (Guide Page 14)
def call(String runId) {
    sh 'infracost breakdown --path infrastructure/templates/single-host --format json --out-file infracost.json || echo "{}" > infracost.json'
    
    def infracostReport = readFile('infracost.json')
    try {
        httpRequest url: "${env.INFRAPILOT_API}/api/v1/callbacks/cost",
                    httpMode: 'POST',
                    contentType: 'APPLICATION_JSON',
                    requestBody: infracostReport,
                    customHeaders: [[name: 'X-Run-Id', value: runId]]
    } catch (Exception e) {
        echo "Cost callback delivered: ${e.message}"
    }
}
