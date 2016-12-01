#!/bin/bash

############################################################################
# @desc: 
#	- 1. create user;
#	- 2. create default directories and authorize;
#	- 3. @usage: sh cachecloud-init.sh [username]
# @author: leifu
# @time: 
###########################################################################

set -o nounset
set -o errexit

readonly redisDir="/opt/cachecloud/redis"
readonly redisTarGz="redis-3.0.7.tar.gz"


# check if the user exists
checkExist() {
	local num=`cat /etc/passwd | grep -w $1 | wc -l`
		 
	#cat /etc/passwd | grep -q "$1"
	if [[ $num == 1 ]]; then
		echo "user $1 exists, overwrite user and *init all data*: [y/n]?"
		read replace
		if [[ ${replace} == "y" ]]; then
			echo "delete existed user: $1."
			userdel -r "$1"
			createUser "$1"
			init "$1"
			return 0
		fi
	else
		createUser "$1"
		init "$1"
	fi
	return 0
}


# create the user
createUser() {
	# create a user 
	useradd -m -d /home/$1 -s /bin/bash $1

	# give the user a password
	passwd $1

	# add the user to sudoers
	#	echo "$1	ALL=(ALL)   ALL" >> /etc/sudoers

	#  Maximum number of days between password change
	chage -M 9999 $1
	echo "OK: create user: $1 done"

}

# create defautl dirs and authorize
init() {
	# create working dirs and a tmp dir
	mkdir -p /opt/cachecloud/data
	mkdir -p /opt/cachecloud/conf
	mkdir -p /opt/cachecloud/logs
	mkdir -p /opt/cachecloud/redis
	mkdir -p /tmp/cachecloud

	# change owner
	chown -R $1:$1 /opt/cachecloud
	chown -R $1:$1 /tmp/cachecloud
	echo "OK: init: $1 done"
}



# install redis 
installRedis() {
	#which redis-server
	#if [[ $? == 0 ]]; then
	#	echo "WARN: redis is already installed, exit."
	#	return
	#fi

	yum install -y gcc
	mkdir -p ${redisDir} && cd ${redisDir}
	wget http://download.redis.io/releases/${redisTarGz} && mv ${redisTarGz} redis.tar.gz && tar zxvf redis.tar.gz --strip-component=1
	make && make install
	if [[ $? == 0 ]]; then
		echo "OK: redis is installed, exit."
		chown -R $1:$1 ${redisDir}
		export PATH=$PATH:${redisDir}/src
		return
	fi
	echo "ERROR: redis is NOT installed, exit."
}

username=$1
checkExist "${username}"
installRedis "${username}"
