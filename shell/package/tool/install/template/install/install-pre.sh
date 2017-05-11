#!/usr/bin/env bash



project_name="${1:-}"
project_label=${project_name:-Unknown Project}


__loglvl2index() {
	local l s="none  NONE  error ERROR warn  WARN  info  INFO  debug DEBUG"
	l=$1 ; l="${s%%$l*}" ; [ "x$l" = "x$s" ] && echo 127 || echo $((${#l}/12))
}
for l in error warn info debug ; do
	[ "x$(type -t "log_${l}")" != xfunction ] && {
		eval "log_${l}() { local lvl=\${MDU_LOG_LEVEL:-\${LOG_LEVEL:-warn}} ; [ \$(__loglvl2index \${lvl}) -lt \$(__loglvl2index ${l}) ] && return 0; echo \"[${l}] \$@\" $([ $(__loglvl2index warn) -ge $(__loglvl2index ${l}) ] && echo ">&2") ; }"
	}
done

