#!/bin/sh
# author zixie

# 函数定义，检测执行结果
checkResult() {
  result=$?
  if [ $result -eq 0 ]; then
    echo "checkResult: execCommand succ, libName:$1"
  else
    echo "!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!"
    echo "checkResult: execCommand failed, libName:$1"
    echo "!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!"
    exit $result
  fi
}

# 合并字符串资源函数
mergeStrings() {
  local source_dir=$1
  local output_dir=$2
  local verbose=${3:-true}  # 默认不显示日志
  local output_file="$output_dir/aaf_merged_strings.xml"

  # 获取文件总数（去掉所有空白字符）
  local total_files=$(find "$source_dir" -type f -name "string*.xml" | wc -l | tr -d '[:space:]')
  local current_file=0

  # 检查源目录是否存在
  if [ ! -d "$source_dir" ]; then
    echo "错误: 源目录不存在: $source_dir" >&2
    exit 1
  fi

  # 创建输出目录
  mkdir -p "$output_dir"
  checkResult

  # 创建 XML 文件头
  echo '<?xml version="1.0" encoding="utf-8"?>' > "$output_file"
  echo '<resources>' >> "$output_file"

  # 合并文件
  find "$source_dir" -type f -name "string*.xml" | sort | while read -r file; do
    current_file=$((current_file + 1))
    [ "$verbose" = "true" ] && echo "正在处理 [$current_file/$total_files] $(basename "$file")"

    # 过滤XML声明和resources标签
    grep -v -e '<?xml version="1.0" encoding="utf-8"?>' \
            -e '<resources>' \
            -e '</resources>' "$file" >> "$output_file"
  done

  # 添加 XML 文件尾
  echo '</resources>' >> "$output_file"

  # 验证输出
  if [ -s "$output_file" ]; then
    echo "合并完成: $total_files 个文件 → $output_file"
  else
    echo "错误: 输出文件为空!" >&2
    exit 1
  fi
}

# 主程序
echo -e "\n\n========== 字符串资源合并开始 ==========\n\n"
localPath=$(pwd)
echo "工作目录: $localPath"

echo "1. 合并中文资源..."
mergeStrings "$localPath/ModelRes/src/main/res/values" "$localPath/bin/values" false

echo "2. 合并英文资源..."
mergeStrings "$localPath/ModelRes/src/main/res/values-en" "$localPath/bin/values-en" false

echo -e "\n\n========== 字符串资源合并完成 ==========\n\n"
echo "生成文件:"
find "$localPath/bin" -name "aaf_merged_strings.xml" -exec ls -lh {} \;