#!/usr/bin/env bash
#
# Please download required files from
# https://huggingface.co/marcoyang/sherpa-ncnn-streaming-zipformer-zh-14M-2023-02-23
#
# Note: epoch-99.pt is a symlink to sherpa-ncnn-streaming-zipformer-zh-14M-2023-02-23/pretrained.pt

python ./pruned_transducer_stateless7_streaming/export-onnx-zh.py \
    --tokens ./pruned_transducer_stateless7_streaming/14M-zh-2023-02-23/tokens.txt \
    --exp-dir ./pruned_transducer_stateless7_streaming/14M-zh-2023-02-23 \
    --use-averaged-model False \
    --epoch 99 \
    --avg 1 \
    --decode-chunk-len 32 \
    --num-encoder-layers "2,3,2,2,3" \
    --feedforward-dims "320,320,640,640,320" \
    --nhead "4,4,4,4,4" \
    --encoder-dims "160,160,160,160,160" \
    --attention-dims "96,96,96,96,96" \
    --encoder-unmasked-dims "128,128,128,128,128" \
    --decoder-dim 320 \
    --joiner-dim 320
