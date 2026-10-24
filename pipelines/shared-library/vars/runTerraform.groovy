// pipelines/shared-library/vars/runTerraform.groovy (Guide Page 14)
def call(String action, String runId, String environment) {
    dir('infrastructure/templates/single-host') {
        if (action == 'plan') {
            sh """
                terraform init -backend=false || true
                terraform plan -var="run_id=${runId}" -var="environment=${environment}" -var="my_ip_cidr=0.0.0.0/0" -out=tfplan || true
                terraform show -no-color tfplan > terraform-plan.txt 2>&1 || true
            """
        } else if (action == 'apply') {
            sh """
                terraform apply -auto-approve tfplan || true
                terraform output -json > outputs.json 2>&1 || true
            """
        } else if (action == 'destroy') {
            sh """
                terraform destroy -auto-approve -var="run_id=${runId}" -var="environment=${environment}" -var="my_ip_cidr=0.0.0.0/0" || true
            """
        }
    }
}
