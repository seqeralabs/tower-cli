
echo "Add AWS credentials"
./towr credentials create aws -n aws -a $AWS_ACCESS_KEY_ID -s $AWS_SECRET_ACCESS_KEY
sleep 5

echo "Create a new AWS Batch compute environment"
./towr compute-envs create aws -n demo -r eu-west-1 -w s3://nextflow-ci/jordeu --max-cpus=123 --fusion
sleep 5

echo "Create a launchpad pipeline"
./towr pipelines create -n sleep_one_minute --params=<(echo 'timeout: 60') https://github.com/pditommaso/nf-sleep
sleep 5

echo "Launch a launchpad pipeline"
./towr launch sleep_one_minute
sleep 5

echo "Launch a launchpad pipeline changing parameters"
./towr launch sleep_one_minute --params=<(echo 'timeout: 30')
sleep 5

echo "Launch a Github pipeline"
./towr launch nextflow-io/hello
sleep 5

echo "Update a pipeline"
./towr pipelines update -n sleep_one_minute --params=<(echo 'timeout: 30')
sleep 5

