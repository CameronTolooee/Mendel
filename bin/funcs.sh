#!/usr/bin/env bash
################################################################################
# funcs.sh - contains functionality used by Mendel shell scripts.
#   Author: Matthew Malensek
# Modified: Cameron Tolooee
################################################################################

# Colorizes text and echoes it to the terminal.
# The variable 'use_color' must be set, otherwise output is not colorized.
# $1 = numeric bash color to use
# Remaining arguments are printed.
colorize() {
    color=${1}
    shift 1

    if [[ ${use_color} == true ]]; then
        echo -en "\e[0;${color}m${@}\e[0m"
    else
        echo -n "${@}"
    fi
}


# Prints a simple status message of the form [hostname] message
print_status() {
    echo -e "[$(hostname)] ${@}"
}


# Performs a "blocking" pkill where the function waits until the process has
# exited before returning.  The arguments for this function are passed directly
# to pkill and pgrep.
#
# Returns nonzero if the process was not found.
blocking_pkill() {
    pkill ${@} &> /dev/null
    if [[ ${?} -ne 0 ]]; then
        # process not found
        return 1
    fi

    # sleep until the process exits
    while pgrep ${@} &> /dev/null; do
        sleep 0.5
    done

    return 0
}
