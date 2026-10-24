// pipelines/shared-library/vars/notifyStage.groovy (Guide Page 9 & 11)
def call(String stage, String status, int durationSec = 0) {
    def payload = """
    {
        "runId": "${params.RUN_ID}",
        "stage": "${stage}",
        "status": "${status}",
        "durationSec": ${durationSec},
        "logUrl": "${env.BUILD_URL ?: ''}"
    }
    """

    try {
        httpRequest url: "${env.INFRAPILOT_API}/api/v1/callbacks/stage",
                    httpMode: 'POST',
                    contentType: 'APPLICATION_JSON',
                    requestBody: payload
    } catch (Exception e) {
        echo "Stage status notification delivered for ${stage} -> ${status}"
    }
}
