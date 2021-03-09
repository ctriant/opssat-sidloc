import time

'''
This script generates training commands that can be sent a running Mochi server.
From the Mochi root directory, the list of generated commands are saved in "test_data/generated.txt".

From the Mochi root directory:

1. Generated the training commands: python3 src/generate.py
2. Start the Online ML server: ./OrbitAI_Mochi
3. Train the models: eval 'echo "reset"; sleep 1; input="test_data/generated.txt"; while IFS= read -r cmd; do sleep 0.02; echo ${cmd}; done < "$input";' | telnet localhost 9999
4. Check that training log file was created: logs/training.csv
5. Infer spacecraft data fetched from WebMUST: eval 'echo "load"; sleep 1; input="test_data/camera_validation_small.txt"; while IFS= read -r line; do sleep 0.02; echo "infer ${line/+/''} $(date +%s%3N)"; done < "$input";' | telnet localhost 9999
6. Check that inference log file was creatd: logs/training.csv
7. Calculate inference/prediction accuracies: python3 src/accurancy.py

'''

# Photodiode 6 elevation threshold for the Camera:
#   - FOV 18.63 deg (in lens specs) and 21 deg (in ICD)
#   - Elevation threshold is 90 deg - (FOV + margin) = 60 deg (1.0472 rad)
PD_ELEVATION_THRESHOLD_HD_CAM = 1.0472

FLOOR = 0.00 # 0 radians.
CEILING = 1.57 # 90 degrees in radian.

pd = FLOOR

# Start time in milliseconds.
# To simulate data input for logging photodiode value acquisition time.
start_time = int(time.time() * 1000)

increment = True

with open("test_data/generated.txt", "w") as data_file: 

    # Generate training input data incrementing from 0 to 90 and then decrementing back to 0.
    while True:

        # Make sure training photodiode data only has 2 digits after the floating point.
        pd = round(pd, 2)

        if pd >= 0:

            # Label '1' means camera can be turned ON.
            # Label '-1' means camera must be turned OFF.
            label = 1 if pd < PD_ELEVATION_THRESHOLD_HD_CAM else -1

            # Write generated trianing command.
            line = 'train ' + str(label) + ' ' + str(pd) + ' ' + str(start_time) + '\n'
            data_file.write(line)
        
        # Exit loop after decrement phase is done.
        if pd < 0:
            break

        # Increment phase.
        elif pd < CEILING and increment:

            # Increment photodiode elevation angle value.
            pd += 0.01

        # Decrement phase.
        else:
            # Start decrementing.
            increment = False
            
            # Decrement photodiode elevation angle value.
            pd -= 0.01

        # Photodiode data acquisition is planned for every 5 seconds.
        start_time += 5000 
