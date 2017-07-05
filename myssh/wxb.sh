#!/bin/bash
comd="`ps aux | grep 111.202.25.55 | grep -v grep`"
if test -z "$comd" 
then
    ssh -fCNR 23345:localhost:22 root@111.202.25.55
fi

