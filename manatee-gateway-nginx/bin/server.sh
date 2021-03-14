#!/bin/bash

#server.sh 逻辑：
##启动: 检查bin目录的pid文件，文件存在，根据pid检查程序是否在运行，在提示。不在调用程序启动命令，
##      并更新pid，控制台信息输出到logs目录的project_console.log。
##      sleep 10秒后检查进程号，在则表示启动成功，不在启动失败
##停止: 检查bin目录的pid文件，文件存在，根据pid检查程序是否在运行，在kill进程号，不在提示；
##      kill进程后sleep 5秒后检查进程号，不在停止成功，在提示停止失败
##状态: 检查bin目录的pid文件，文件存在，根据pid检查程序是否在运行，在提示已运行，不在提示进程不在
##重启：先调用stop，再调用start

##odmspy调用该脚本，获取脚本屏幕输出并展示，输出信息应当尽量简洁易懂,
##如启动有异常，请到后台程序logs目录查看console.log启动日志

##本脚本仅针对内部标准java程序，实际启动的参数可能有所不同，请开发根据实际情况修改
##1、29行MAIN_CLASS必须改，改为实际程序的main名称
##2、32行JVM_ARGS根据实际情况调整
##3、如果需要添加其他启动参数，请一并修改85行nohup启动命令
##4、脚本不能含有tail -f 的命令
##5、其它特殊情况，具体讨论，可以联系陈福钊、蔡柱昌

## update by hz15111811 2017-01-17
## 1、修改了MAIN_CLASS
## 2、修改了JVM_ARGS
## 3、修改了CLASSPATH加载lib方式
## 4、修改stop
## 5、修改程序启动参数 删除logback

###################初始化###############################
cd $(cd "$(dirname "$0")"; pwd)/../
SHELL_PROG=./server.sh

#程序启动参数相关变量
MAIN_CLASS=com.ranhy.example.manatee.gateway.nginx.CatfishGatewayNginxApplication
#LANG="zh_CN"
JVM_ARGS="-server -Xms2048m -Xmx2048m -XX:PermSize=128m -XX:MaxPermSize=512m -Xss512k -XX:SurvivorRatio=8 -XX:MaxTenuringThreshold=7 -XX:GCTimeRatio=19 -Xnoclassgc -XX:+DisableExplicitGC -XX:+UseParNewGC -XX:+UseConcMarkSweepGC -XX:+CMSPermGenSweepingEnabled -XX:+UseCMSCompactAtFullCollection -XX:CMSFullGCsBeforeCompaction=0 -XX:+CMSClassUnloadingEnabled -XX:+CMSParallelRemarkEnabled -XX:CMSInitiatingOccupancyFraction=70 -XX:SoftRefLRUPolicyMSPerMB=0 -Xloggc:./logs/gc.log -XX:+PrintGCDetails -XX:+PrintGCTimeStamps -XX:+PrintGCApplicationStoppedTime -XX:+PrintGCApplicationConcurrentTime -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=./java_pid.hprof"

##HOMES
CLASSPATH=$PWD
PROJECT=$(basename $PWD)
BIN_HOME=$PWD/bin
LIB_HOME=$PWD/lib
LOG_HOME=$PWD/logs
PIDFILE=$BIN_HOME/server.pid
CONSOLE=$LOG_HOME/${PROJECT}-console.log

#启动自动检测目录是否存在
test -d $LIB_HOME || { echo "$LIB_HOME not exits" ; exit 1; }
test -d $BIN_HOME || { echo "$BIN_HOME not exits" ; exit 1; }
#第一次部署启动时，自动创建logs目录
test -d $LOG_HOME || mkdir -p $LOG_HOME

## file jar loading
CLASSPATH=$CLASSPATH:$LIB_HOME/*
export CLASSPATH

#程序启动后，sleep 一定时间再检查进程是否存在
START_SLEEP_TIME=10
#程序停止后，sleep一定时间再检查进程是否存在
STOP_SLEEP_TIME=5
#######################################################


function getpid(){
    if [[ -f $PIDFILE ]]
    then
        pid=$(cat $PIDFILE|awk '{print $1}')
        #java程序启动带家目录
        num=$(ps aux|grep -v grep|awk '{print $2}'|grep -w $pid|wc -l)
        #根据文件pid检查是否程序正在运行，如果一致，则正在运行
        if [[ $num -eq 1 ]]
        then
            echo $pid
        else
            echo "-1"
        fi
    else
         echo "-1"
    fi
}


#程序启动,开发可以自动启动与程序判断内容
function startserver(){
    #cd $LIB_HOME
    nohup java $JVM_ARGS -cp $CLASSPATH $MAIN_CLASS >$CONSOLE 2>&1  &
    pid=$!
    echo $pid > $PIDFILE

    #启动后休眠10秒,检查pid, 进程在表示成功
    sleep $START_SLEEP_TIME
    num=$(ps aux|grep -v grep|awk '{print $2}'|grep -w $pid|wc -l)

    if  [[ $num -gt 0 ]]
    then
        echo $pid
    else
        echo "-1"
    fi
}

function start(){
    #根据pid检查程序是否在运行，进程号小于0(0代表进程没启动)，则启动程序
    #否则，程序已运行，保留现状，返回提示
    oldpid=$(getpid)
    if [[ $oldpid -lt 0 ]]
    then
        echo "$PROJECT Starting..."
        pid=$(startserver)
        if [[ $pid -gt 0 ]]
        then
            echo "[$(date '+%Y-%m-%d %T')] Startup $PROJECT success.( pid: $pid )"
        else
            echo "[$(date '+%Y-%m-%d %T')] Startup $PROJECT fail."
        fi
    else
        echo "[$(date '+%Y-%m-%d %T')] $PROJECT is running aleady. ( pid: $oldpid )"

    fi
}

function stop(){

    pid=$(getpid)
    #pid存在，则kill，不存在，提示
    if [[ $pid -gt 0 ]]
    then
        echo "$PROJECT stoping..."
        kill $pid
        sleep $STOP_SLEEP_TIME
        num=$(ps aux|grep -v grep|awk '{print $2}'|grep -w $pid|wc -l)
        #根据pid检查进程数量，0停止成功,大于0停止失败
        if [[ $num -eq 0 ]]
        then
            echo "[$(date '+%Y-%m-%d %T')] Stop $PROJECT success."
            rm -f $CONSOLE
        else
            echo "[$(date '+%Y-%m-%d %T')] Stop $PROJECT fail.( pid:$pid )"
        fi
    else
        echo "[$(date '+%Y-%m-%d %T')] $PROJECT is not running"
        # exit 0;
    fi
}


function restart(){
    stop
    start
}

function status() {
    pid=$(getpid)

    if [[ $pid -gt 0 ]]
    then
        echo "[$(date '+%Y-%m-%d %T')] $PROJECT is running.( pid:$pid )"
    else
        echo "[$(date '+%Y-%m-%d %T')] $PROJECT is not running"
    fi
}


case "$1" in
    start)
       start
       exit $?
       ;;
    stop)
        stop
        exit $?
        ;;
    restart)
        stop
        start
        exit $?
        ;;
    status)
        status
        exit $?
        ;;
    *)
        echo "Usage: bash $SHELL_PROG {start|status|stop|restart}"
        exit 1
        ;;
esac
exit 0

