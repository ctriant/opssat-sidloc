#!/bin/bash

# Process a batch of train and infer commands written in a file and calculate perfomance metrics.
# Each line in the given file must contain a single command. First the train commands followed by the infer commands.
#
# Sample command files generated by generate.py:
#   - cmds/generated_A.txt
#   - cmds/generated_B.txt
#   - cmds/generated_B.txt
#
# Usage: ./benchmark.sh <int:epochs> <float:sleep> <string:commands_filename>
#   - epochs: the number of training epochs.
#   - sleep: sleep time between each commands sent.
#   - commands_filename: path to the file listing all the commands to send to the Mochi server.
#
# e.g.: ./benchmark.sh 10 0.03 cmds/generated_A.txt

if [ $# -ne 3 ]; then
    echo "Invalid number of parameters. Usage: ./benchmark.sh <int:epochs> <float:sleep> <string:commands_filename>"
    exit 1
fi

# The training epoch loop.
for EPOCH in $(seq 1 1 $1)
do
    # The classification metrics pull data from the inference.csv log file.
    # Make sure this file is deleted in so that the metrics calculated for the updated version of the model doesn't take into account
    # inference results from the previous version of the same model.
    rm logs/inference.csv

    # Go ahead and delete training.csv log file as well. No need to but do it anyway.
    rm logs/training.csv

    # Start the Mochi server.
    ../OrbitAI_Mochi &

    # Wait a bit to make sure that the Mochi server has started nicely.
    sleep 1

    # Send training and inference commands to the Mochi server.
    if [ $EPOCH -eq 1 ]; then
        # First training epoch must be preceeded by a reset command.
        eval 'echo "reset"; sleep 1; input="$3"; while IFS= read -r cmd; do sleep $2; echo ${cmd}; done < "$input"; echo "exit"' | telnet localhost 9999
    else
        # All subsequent epochs must be precesed by a load command.
        eval 'echo "load"; sleep 1; input="$3"; while IFS= read -r cmd; do sleep $2; echo ${cmd}; done < "$input"; echo "exit"' | telnet localhost 9999
    fi

    # Caculate classification metrics of the trained models.
    python3 analyze.py 0 logs/inference.csv metrics/ground/epochs_$EPOCH.csv
done





