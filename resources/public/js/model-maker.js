function draw() {
    var minx=parseInt($("#minx").val());
    var maxx=parseInt($("#maxx").val());
    try {
        functionPlot({
            target: "#plot",
	    xAxis:{domain:[minx-1, maxx+1]},
            data: [{fn: $("#eq").val(),
		    sampler: "builtIn",  //make function-plot use the evaluator of math.js
		    graphType: "polyline"}
		  ],
	    annotations:[
		{x: maxx, text:"Max X"},
		{x: minx, text:"Min X"}
	    ]
	   
        });
    }
    catch (err) {
        console.log(err);
        alert(err);
    }
}

$("#draw").click(function (event) {
    event.preventDefault();
    draw();
});

$("#coords").click(function(event){
    var minx=parseInt($("#minx").val());
    var maxx=parseInt($("#maxx").val());
    var N=parseInt($("#number").val());
    var vscale=round(parseFloat($("#vscale").val()));
    var name=$("#name").val()
    var str="";
    //str+="a:"+minx+" b:"+maxx+"\n";
    var shape;
    var fun=(e)=>math.eval($("#eq").val(),{x:e});
    if($("input:checked").val()=="rtrig")
	shape=makeRTrig(minx, maxx, N, fun);
    if($("input:checked").val()=="square")
	shape=makeSquare(minx, maxx, N, fun);
    shape.v=shape.v.map((e)=>[e[0],e[1],e[2]*vscale])
    //str+="v:"+shape.v+"\n";
    str+="f:"+shape.f+"\n";
    shape.name=name;
    var s={};
    s.name=shape.name;
    s.f=shape.f;
    s.v=shape.v;
    s.user=$.cookie("uname");
    stitch.StitchClientFactory.create("calcgoggles-qwpga")
	.then((client)=>console.log("sucess!",client))
	.catch(console.err);
    /*$.post("/push/model", {"shape":s})
	.done(function(){
	    window.location.href="/view-model/"+shape.name;
	})
	.fail(function(){
	    alert(shape.name+" is too fat :C try using a smaller amount of cross sections")
	    });*/
    
});
function round(n){
    return Number(n.toFixed(4));
}

function makeBase(a, b, N, fun){
    var dx=round((b-a)/N);
    //make x set
    var x=[];
    for(i=0,j=a;i<N;i++,j+=dx)
	x.push([round(j),0,0]);
    x.push([b,0,0]);
    //make y set
    var y=[];
    x.forEach((e)=>y.push([e[0],round(fun(e[0])),0]));
    v=[];
    x.forEach((e)=>v.push(e));
    y.forEach((e)=>v.push(e));
    var f=[];
    f=baseFaces(N, f);
    return {"dx":dx,"x":x, "y":y, "v":v, "f":f};
}

function makeRTrig(a, b, N, fun){
    var shape=makeBase(a, b, N, fun);
    //z remaps y->z
    shape.z=[];
    shape.y.forEach((y)=>{
	shape.z.push([y[0],0,y[1]]);
	shape.v.push([y[0],0,y[1]])
    });
    //make end capping faces
    shape.f=trig(0,N+1,(N+1)*2, shape.f);
    shape.f=trig(N,N+N+1,N+2*(N+1), shape.f);

    shape.f=makeLegs(N, shape.f);
    shape.f=generateHyp(N, shape.f);
    return shape;
}

function trig(a, b, c, f){
    f.push([a,b,c]);
    return f;
}

function quad(a, b, c, d, f){
    f=trig(a, b, c, f);
    //zig zag so they are same rotation point to point
    f=trig(d, c, b, f);
    return f;
}
function baseFaces(N, f){
    for(i=0;i<N;i++)
	f=quad(i,i+1,N+i+1,N+i+2,f);
    return f;
}

function makeLegs(N, f){
    for(i=0;i<N;i++)
	f=quad(i,i+1,2*(N+1)+i,2*(N+1)+i+1,f);
    return f;
}

function generateHyp(N, f){
    //make an entrance
    //bumpin entrance music
    //work the crowd
    //mabe some explosions
    for(i=0;i<N;i++)
	f=quad(N+i+1,N+i+2,2*(N+1)+i,2*(N+1)+i+1,f);
    return f;
}


function makeSquare(a, b, N, fun){
    var shape=makeBase(a, b, N, fun);
    //z remaps y->z
    shape.z=[];
    shape.y.forEach((y)=>{
	shape.v.push([y[0],0,y[1]]);
    });

    //creates square with x,y,z
    shape.w=[]
    shape.y.forEach((y)=>{
	shape.v.push([y[0],y[1],y[1]])
    });
    
    
    //make end capping faces
    shape.f=quad(0,N+1,(N+1)*2, (N+1)*3, shape.f);
    shape.f=quad(N,N+N+1,N+2*(N+1), N+(N+1)*3, shape.f);

    shape.f=makeLegs(N, shape.f);
    shape.f=makeSquareSide(N, shape.f);
    shape.f=makeSquareTop(N, shape.f);
    return shape;
}

function makeSquareSide(N, f){
    for(i=0;i<N;i++)
	f=quad(N+i+1,N+i+2,3*(N+1)+i,3*(N+1)+i+1,f);
    return f;
}
function makeSquareTop(N, f){
    for(i=0;i<N;i++)
	f=quad(2*(N+1)+i,2*(N+1)+i+1,3*(N+1)+i,3*(N+1)+i+1,f);
    return f;
}

draw();
