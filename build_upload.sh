#!/bin/sh
# author zixie

#函数定义，检测执行结果
function checkResult() {
  result=$?
  echo "result : $result"
  if [ $result -eq 0 ]; then
    echo "checkResult: execCommand succ"
  else
    echo "checkResult: execCommand failed"
    exit $result
  fi
}

deleteempty() {
  find ${1:-.} -mindepth 1 -maxdepth 1 -type d | while read -r dir; do
    if [[ -z "$(find "$dir" -mindepth 1 -type f)" ]] >/dev/null; then
      echo "$dir"
      rm -rf ${dir} 2>&- && echo "Empty, Deleted!" || echo "Delete error"
    fi
    if [ -d ${dir} ]; then
      deleteempty "$dir"
    fi
  done
}
echo "********APK build init set env *******"
localPath=$(pwd)
echo "localPath:"$localPath
libName=$1
version=$2
echo "libName:"$libName
echo "version:"$version

hasNotCommit=$(git status | grep "what will be committed" | wc -l)
if [ $hasNotCommit -gt 0 ]; then
  echo "------------- git has code not commit !!!!!!!!!!!! -------------"
  exit
fi

if [ "$libName"x = ""x ]; then
  echo "------------- libName can not be null !!!!!!!!!!!! -------------"
  exit
fi

if [ "$version"x = ""x ]; then
  echo "------------- version can not be null !!!!!!!!!!!! -------------"
  exit
fi

echo "******** build mkdir bin *******"
cd $localPath
#临时文件
if [ ! -d "$localPath/bin" ]; then
  mkdir "$localPath/bin"
fi
rm -fr $localPath/bin/*

src=" *ext.mainProject *= *\\\""
dst="ext.mainProject = \\\"${libName}\\\""
cat $localPath/dependencies.gradle | sed "/$src/s/.*/$dst/" >$localPath/bin/dependencies.gradle
mv -f $localPath/bin/dependencies.gradle $localPath/dependencies.gradle

src=" *ext.developModule *= *\\\""
dst="ext.developModule = \\\"${libName}\\\""
cat $localPath/dependencies.gradle | sed "/$src/s/.*/$dst/" >$localPath/bin/dependencies.gradle
mv -f $localPath/bin/dependencies.gradle $localPath/dependencies.gradle


src=" *ext.includeALLDependOnDevelopModule *= *"
dst="ext.includeALLDependOnDevelopModule = false"
cat $localPath/dependencies.gradle | sed "/$src/s/.*/$dst/" >$localPath/bin/dependencies.gradle
mv -f $localPath/bin/dependencies.gradle $localPath/dependencies.gradle

src=" *ext.moduleVersionName *= *"
dst="ext.moduleVersionName = \\\"${version}\\\""
cat $localPath/dependencies.gradle | sed "/$src/s/.*/$dst/" >$localPath/bin/dependencies.gradle
mv -f $localPath/bin/dependencies.gradle $localPath/dependencies.gradle



src="[0-9]*\.[0-9]*\.[0-9]*"
cat $localPath/dependencies.gradle | sed "/ *\\\"${libName}\\\" *: */,/version/s/${src}/${version}/"  >$localPath/bin/dependencies.gradle
mv -f $localPath/bin/dependencies.gradle $localPath/dependencies.gradle
if [[ $libName == Router* ]]; then
  ./gradlew clean assemble publish
else
  ./gradlew clean assembleRelease publish
fi
checkResult
#./gradlew clean

src=" *ext.mainProject *= *\\\""
dst="ext.mainProject = \\\"APPTest\\\""
cat $localPath/dependencies.gradle | sed "/$src/s/.*/$dst/" >$localPath/bin/dependencies.gradle
mv -f $localPath/bin/dependencies.gradle $localPath/dependencies.gradle
checkResult

src=" *ext.developModule *= *\\\""
dst="ext.developModule = \\\"Application\\\""
cat $localPath/dependencies.gradle | sed "/$src/s/.*/$dst/" >$localPath/bin/dependencies.gradle
mv -f $localPath/bin/dependencies.gradle $localPath/dependencies.gradle
checkResult

git add $localPath/dependencies.gradle
git commit $localPath/dependencies.gradle -m"auto add ${libName} to version ${version} by build.sh, author:zixie "
checkResult
