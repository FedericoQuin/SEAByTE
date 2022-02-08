


class AdaptationStatus {
    constructor(messageA, messageB, deployedSetup, currentExperiment, userProfile, test, history) {
        this.messageA = messageA;
        this.messageB = messageB;
        this.currentSetup = deployedSetup;
        this.currentExperiment = currentExperiment;
        this.historyExperiments = [];
        this.userProfile = userProfile;
        this.currentTest = test;
        this.history = history
    }

    render() {
        let historyString = this.history.map(x => `- ${x.name} (${x.type})`).join('<br>');

        return `<div style="margin: 5px 0px 5px 5px; width: 100%; max-width: 900px;">
        <label class="huge">Status feedback loop:</label>
            <div class="grid-container" id="status-ab-container" style="font-size: large; gap: 15px;">
                <div class="grid-item"><label>Data samples collected for variant A:</label></div>
                <div class="grid-item"><label>${this.messageA}</label></div>

                <div class="grid-item"><label>Data samples collected for variant B:</label></div>
                <div class="grid-item"><label>${this.messageB}</label></div>
                
                <div class="grid-item"><label>Current setup:</label></div>
                <div class="grid-item"><label>${this.currentSetup}</label></div>
                
                <div class="grid-item"><label>Current experiment:</label></div>
                <div class="grid-item"><label>${this.currentExperiment}</label></div>
                
                <div class="grid-item"><label>Current user profile:</label></div>
                <div class="grid-item"><label>${this.userProfile}</label></div>
                
                <div class="grid-item"><label>Statistical test to check:</label></div>
                <div class="grid-item"><label>${this.currentTest}</label></div>
                
                <div class="grid-item"><label>History experiments:</label></div>
                <div class="grid-item"><label>${historyString}</label></div>
            </div>
        </div>`;
    }
}


function updateFeedbackLoopData(data) {
    // console.log(data);
    const adaptationStatus = new AdaptationStatus(data.messageA, data.messageB, 
        data.deployedSetup, data.currentExperiment, data.userProfile, data.currentTest, data.history);
    const element = adaptationStatus.render();

    const parent = document.getElementById('status-ab');
    parent.innerHTML = element;
}

window.pollFeedbackLoop = () => {
    (async () => {
        let result = await fetch('/adaptation/status');

        if (result.status == 200) {
            let data = await result.json();
            updateFeedbackLoopData(data);
        }
    })();
}


function drawBoxPlots(data) {
    let processedData = [
        {x: data.B, type: 'box', name: 'B'},
        {x: data.A, type: 'box', name: 'A'}
    ]

    const layout = {
        title: data.title,
        paper_bgcolor: 'rgba(0,0,0,0)',
        plot_bgcolor: 'rgba(0,0,0,0)',
        titlefont: {size: 18},
        legend: {traceorder: 'reversed'}
    }

    Plotly.newPlot('boxplot-status', processedData, layout);
}

function drawBarChart(data) {
    let processedData = [
        {x: ['A', 'B'], y: [data.positivesA, data.positivesB], name: data.positiveName, type: 'bar'},
        {x: ['A', 'B'], y: [data.negativesA, data.negativesB], name: data.negativeName, type: 'bar'}
    ]
      
    var layout = {
        title: data.title,
        barmode: 'group',
        paper_bgcolor: 'rgba(0,0,0,0)',
        plot_bgcolor: 'rgba(0,0,0,0)',
        titlefont: {size: 18}
    };
    
    Plotly.newPlot('boxplot-status', processedData, layout);
}


window.updateBoxPlots = () => {
    // drawBarChart({
    //     positivesA: 10,
    //     positivesB: 20,
    //     negativesA: 50,
    //     negativesB: 40,
    //     positiveName: 'Clicked on recommendation',
    //     negativeName: 'Ignored recommendation',
    //     title: 'Generated clicks (number of requests)'
    // });
    // drawBoxPlots({
    //     A: [10, 15, 13, 14, 20, 19],
    //     B: [10, 8, 13, 14, 15, 9],
    //     title: 'Response time (ms)'
    // });
    (async () => {
        let result = await fetch('/adaptation/plotData');

        if (result.status == 200) {
            let data = await result.json();
            if (Object.keys(data).length === 0) {
                // Ignore, no results to plot
            } else {
                switch(data.type) {
                    case "box":
                        drawBoxPlots(data);
                        break;
                    case "bar":
                        drawBarChart(data);
                        break;
                    default:
                        console.log(`Unsupported chart type: ${data.type}`)
                        return;
                }
            }
        }
    })();
}





