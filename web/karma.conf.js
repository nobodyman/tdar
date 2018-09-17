// Karma configuration
var webpackConfig = require('./webpack-test.config.js');
var fs = require("fs");


var specFiles = "src/test/frontend/spec/**/*.js";

console.log("*************************************** ");
console.log("**** to run a single test.         **** ");
console.log("**** karma start -- VueBalkSpec.js ****")
console.log("**** or          -- Vue            ****")
console.log("*************************************** ");

if (process.argv != undefined  && process.argv.length > 0) {
    var last = process.argv[process.argv.length-1];
    if (last != 'start' && last != 'run' && last != 'true') {
        if (!last.endsWith(".js")) {
            last = last + "*";
        }
        specFiles = "src/test/frontend/spec/**/*" + last;
    }
}


module.exports = function(config) {
    config.set({
       
        webpack: webpackConfig,
        browserConsoleLogOptions: {terminal:false},

        // base path that will be used to resolve all patterns (eg. files, exclude)
        basePath: '',

        // frameworks to use
        // available frameworks: https://npmjs.org/browse/keyword/karma-adapter
        frameworks: ['jasmine-ajax', 'jasmine-jquery', 'jasmine'],

        // list of files / patterns to load in the browser (we aren't using requireJS, so order matters)
        files: [].concat(
            [
                // app dependencies  (included in DOM served by karma, but not monitored for changes)
                /*'node_modules/es6-promise/dist/es6-promise.js',
                'node_modules/es6-promise/dist/es6-promise.auto.js',
                'node_modules/vue/dist/vue.js',
                'node_modules/axios/dist/axios.js',*/
                'node_modules/moxios/dist/moxios.js',
                
                /*{pattern: "src/main/webapp/components/jquery/dist/jquery.js", watched: false},
                {pattern: "src/main/webapp/includes/jquery-ui-1.11.4/jquery-ui.min.js", watched: false},
                {pattern: "src/main/webapp/includes/jquery-ui-1.11.4/jquery-ui.min.css", watched: false},
                {pattern: "src/main/webapp/includes/modernizr-custom-2.6.2.min.js", watched: false},
                {pattern: "src/main/webapp/includes/jquery.validate-1.13.1/jquery.validate.js", watched: false},
                {pattern: "src/main/webapp/includes/jquery.validate-1.13.1/additional-methods.js", watched: false},*/
                {pattern: "src/main/webapp/js_includes/includes/bootstrap-2.32/js/bootstrap.js", watched: false},
                {pattern: "src/main/webapp/js_includes/includes/bootstrap-2.32/css/bootstrap.css", watched: false},


            ],
            [
                
                //specs
                specFiles,
                //"src/test/frontend/spec/WorldMapSpec.js",  

                // jasmine fixtures - added to DOM when you call loadFixtures(filename) in your test
                {pattern:"src/test/frontend/fixtures/**/*.html", watched:true, served:true, included:false},
                // more fixtures - added to DOM when you call loadFixtures(filename) in your test
                {pattern:"src/main/webapp/WEB-INF/content/**/*.html", watched:true, served:true, included:false},

                // html2js fixtures - globally accessible via  window.__html__[filepath]
                "src/test/frontend/html2js/**/*.html",

                //static files: served by karma webserver but not included on page
                {pattern: "src/main/webapp/images/**/*", served:true, included:false, watched:false},


                //static files: images used by js libraries, e.g. jquery-ui, jquery-file-upload
                {pattern: "src/main/webapp/includes/**/images/**/*", served:true, included:false, watched:false},
                {pattern: "src/main/webapp/includes/**/img/**/*", served:true, included:false, watched:false},
                {pattern: "src/main/webapp/js_includes/components/**/*.*", served:true, included:false, watched:false},
                {pattern: "src/main/webapp/js/maps/**/*.*", served:true, included:false, watched:false},
                {pattern: "src/main/webapp/dist/templates/*.*", served:true, included:false, watched:false},
            ]),

        // certain html and css files may expect static resources at specific urls (e.g. /images/foo.gif)
        proxies: {
            '/images/': '/base/src/main/webapp/images/',
            '/dist/': '/base/src/main/webapp/dist/',
            '/includes/': '/base/src/main/webapp/js_includes/includes/',
            '/js/maps/': '/base/src/main/webapp/js/maps/'
        },
            ///Users/jimdevos/develop/tdar.src/src/main/webapp/js/maps/world.json

        // list of files to exclude that would otherwise get picked up by the config.files patterns
        exclude: [],

        // preprocess matching files before serving them to the browser
        // available preprocessors: https://npmjs.org/browse/keyword/karma-preprocessor
        preprocessors: {
            //caveat: files deeclared here cannot also be used as jasmine fixtures (known bug)
            //TODO: do we need both jasmine + htmljs fixtures? Figure out advantages/disadvantages of each
            'src/test/frontend/html2js/*.html': ['html2js']
            ,'src/main/webapp/js/**/*.js': ['coverage'],
            'src/test/frontend/spec/**/*.js' : ['webpack']
        },

        // test results reporter to use
        // possible values: 'dots', 'progress'
        // available reporters: https://npmjs.org/browse/keyword/karma-reporter
        reporters: ['progress',"junit",'coverage',"spec"],
        junitReporter: {
            outputDir: 'target/karma/reports/',
            suite: 'models'
        },
        specReporter: {
            maxLogLines: 5,             // limit number of lines logged per test
            suppressErrorSummary: false, // do not print error summary
            suppressFailed: false,      // do not print information about failed tests
            suppressPassed: false,      // do not print information about passed tests
            suppressSkipped: true,      // do not print information about skipped tests
            showSpecTiming: false,      // print the time elapsed for each spec
            failFast: false              // test would finish with error when a first fail occurs. 
          },

	    coverageReporter: {
	        type : 'html',
	        dir : 'target/karma/coverage/'
	    },
        // web server port
        port: 9876,

        // enable / disable colors in the output (reporters and logs)
        colors: true,

        // level of logging
        // possible values: config.LOG_DISABLE || config.LOG_ERROR || config.LOG_WARN || config.LOG_INFO || config.LOG_DEBUG
        logLevel: config.LOG_DEBUG,

        // enable / disable watching file and executing tests whenever any file changes
        autoWatch: true,

        // start these browsers
        // available browser launchers: https://npmjs.org/browse/keyword/karma-launcher

         browsers: ['ChromeHeadless'],
        //browsers: ['PhantomJS'],
        // concurrency: Infinity,
        // Continuous Integration mode
        // if true, Karma captures browsers, runs the tests and exits
        singleRun: true,
        
        customLaunchers: {
           ChromeHeadless: {
             base: 'Chrome',
             flags: ['--headless', '--disable-gpu', '--remote-debugging-port=9222']
           }
         },
         
        client: {
            captureConsole: true,
            config: {
                browserConsoleLogOptions: true
            }
        },
        browserConsoleLogOptions: {
            terminal: true,
            level: ""
        }
    });
};
