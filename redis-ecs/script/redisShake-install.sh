#!/bin/bash

############################################################################
# @desc:
#	- 1. install redis-shake;
#	- 2. @usage: sh redisShake-install.sh [username]
###########################################################################

set -o nounset
set -o errexit

# install redis-shake
installRedisShake() {
   local redisShakeDir="/opt/cachecloud/redis-shake-2.0.3"
   local redisShakeTarGz="redis-shake-v2.0.3.tar.gz"
	 mkdir -p ${redisShakeDir} && cd ${redisShakeDir}
   wget https://github.com/alibaba/RedisShake/releases/download/release-v2.0.3-20200724/${redisShakeTarGz} && tar zxvf ${redisShakeTarGz} --strip-component=2
   if [[ $? == 0 ]]; then
    echo -e "\033[41;36m OK: ${redisShakeTarGz} is installed, exit. \033[0m"
		chown -R $1:$2 ${redisShakeDir}
		return
	 fi
	 echo -e "\033[41;36m ERROR: ${redisShakeTarGz} is NOT installed, exit. \033[0m"
}


# main entrance
username="cachecloud-open"
password="cachecloud-open"
if [[ $# > 0 && -n "$1" ]]; then
  username="$1"
  echo -e "\033[41;36m please input password for user: ${username} \033[0m"
  stty -echo
  read password
  stty echo
fi
echo -e "\033[41;36m use username: ${username} \033[0m"
installRedisShake "${username}" "${password}"
