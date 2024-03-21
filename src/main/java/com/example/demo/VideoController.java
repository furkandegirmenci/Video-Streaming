package com.example.demo;

import com.github.kokorin.jaffree.StreamType;
import com.github.kokorin.jaffree.ffmpeg.FFmpeg;
import com.github.kokorin.jaffree.ffmpeg.PipeOutput;

import lombok.extern.log4j.Log4j2;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/video/{id}")
@Log4j2
public class VideoController {

    @GetMapping(value = "/live.mp4")
    @ResponseBody
    public ResponseEntity<StreamingResponseBody> livestream(@PathVariable("id") String tipperId) throws Exception {

        String rtspUrl = "udp://127.0.0.1:8080/vid";
        ResponseEntity<StreamingResponseBody> entity = ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(os -> {
                    FFmpeg.atPath()
                            .addArgument("-re")
                            .addArguments("-acodec", "pcm_s16le")
                            .addArguments("-i", "/dev/video0")
                            .addArguments("-tune", "zerolatency")
                            .addArguments("-b:a", "5k" )
                            .addArguments("-b:v", "5000K")
                            .addArguments("-vf", "scale=160x120")
                            .addArguments("-f", "mpegts")
                            .addArgument(rtspUrl)
                            .addOutput(PipeOutput.pumpTo(os)
                                    .disableStream(StreamType.AUDIO)
                                   .disableStream(StreamType.SUBTITLE)
                                    .disableStream(StreamType.DATA)
                                    .setFrameCount(StreamType.VIDEO, 100000L)
                                    //1 frame every 10 seconds
                                    .setFrameRate(30)
                                    .setDuration(500, TimeUnit.SECONDS)
                                    .setFormat("ismv"))
                            .addArgument("-nostdin")
                            .execute();
                });

        return entity;
    }
}
