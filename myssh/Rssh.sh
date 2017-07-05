#!/bin/bash


date 

ssh -fCNR 10101:localhost:22 root@139.199.119.82
#autossh -M 1221 -f -N -R 22345:localhost:22 root@139.199.119.82

