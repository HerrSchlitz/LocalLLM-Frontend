# agent_server.py (fully fixed for Windows + Python 3)
from fastapi import FastAPI, HTTPException, Request
from pydantic import BaseModel
import uvicorn
import requests  # for LM Studio health check if needed 
app = FastAPI(title="Local Agentic Office API")

class ExecuteRequest(BaseModel):
    text: str

# from Crewflow import Agent, Task, LLM  
File "C:\Users\botte\agent_server.py"
# Agent definitions (reuse as-is from original context)
local_llm = LLM, (
    model="openai/qwen3-3b-hivemind-instruct-heretic-abliterated-uncensored-neo-imatrix", 
    base_url="http://100.94.92.144:42069",
    api_key="sk-lm-9kcmNqNc:5SzCDCMhKoV0clFVWIoY"
)

receptionist = Agent(...)
researcher = Agent(...)
strategist = Agent(...)
writer = Agent(...)
editor = Agent(...)
archivist = Agent(...)


tasks = [
    Task(description=f"Parse this request: '{input_text}'", expected_output="Clean summary.", agent=receptionist),
    Task(description="Research hardware specs or software settings relevant to the summary.", expected_output="Fact sheet.", agent=researcher),
    Task(description="Create a step-by-step implementation plan from the facts.", expected_output="Action blueprint.", agent=strategist),
    Task(description="Draft the technical guide or checklist based on the blueprint.", expected_output="Raw draft text.", agent=writer),
    Task(description="Review and polish the draft for maximum readability.", expected_output="Polished technical text.", agent=editor),
    Task(description="Format the final output into a clean, markdown-compatible report file.", expected_output="Final structured markdown.", agent=archivist),
]

@app.post("/execute_task")
async def execute_agent(request: ExecuteRequest):
    input_text = request.text.strip()
    
    if not input_text:
        raise HTTPException(status_code=400, detail="Input text cannot be empty.")
    
    print(f"\n🔄 AGENT FAMILY:Processing task for: '{input_text[:50]}...'")
    
    crew = Crew(
        agents=[receptionist, researcher, strategist, writer, editor, archivist],
        tasks=[t1, t2, t3, t4, t5, t6],
        process=Process.sequential
    )
    
    try:
        result = crew.kickoff()
        print(f"✅ Output generated: {len(str(result))} chars")
        
        # Optionally log to file for debugging:
        with open("agent_logs.txt", "a", encoding="utf-16le") as log:  # Windows-safe encoding
            log.write(f"[{input_text[:70]}... -> len={len(str(result))}] \n")

        return {"result": str(result)}

    except Exception as e:
        print(f"❌ Task failed: {e}")
        raise HTTPException(status_code=500, detail=str(e))

if __name__ == "__main__":
    uvicorn.run(app, host="0.0.0.0", port=0)
