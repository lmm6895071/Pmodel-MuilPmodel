#!/bin/bash
comd="`ps aux | grep 139.199.119.82 | grep -v grep`"
if test -z "$comd" 
then
    ssh -fCNR 13345:localhost:22 root@139.199.119.82
fi

