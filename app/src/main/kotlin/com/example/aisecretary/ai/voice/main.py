import random
import datetime
import webbrowser
import os
import requests

def speak(text):
    # Simple print for testing; replace with TTS later
    print(text)

def handle_command(command):
    command = command.lower()

    # ------------------------
    # Beginner-Friendly Commands
    # ------------------------
    if "tell me a joke" in command:
        jokes = [
            "Why did the computer show up at work late? It had a hard drive!",
            "Why do programmers prefer dark mode? Because light attracts bugs!",
            "Why did the AI go to school? To improve its neural network!"
        ]
        speak(random.choice(jokes))

    elif "what's the time" in command:
        now = datetime.datetime.now()
        speak(f"The time is {now.hour}:{now.minute}")

    elif "open youtube" in command:
        speak("Opening YouTube")
        webbrowser.open("https://www.youtube.com")

    elif "open google" in command:
        speak("Opening Google")
        webbrowser.open("https://www.google.com")

    elif "how are you" in command:
        responses = [
            "I’m doing great, thank you!",
            "I’m fine, how about you?",
            "Feeling ready to assist you!"
        ]
        speak(random.choice(responses))

    elif "weather in" in command:
        try:
            city = command.split("weather in")[-1].strip()
            api_key = "<YOUR_OPENWEATHERMAP_API_KEY>"
            url = f"http://api.openweathermap.org/data/2.5/weather?q={city}&appid={api_key}&units=metric"
            response = requests.get(url).json()
            if response.get("main"):
                temp = response["main"]["temp"]
                speak(f"The temperature in {city} is {temp}°C")
            else:
                speak("I couldn't find the weather for that city.")
        except:
            speak("Sorry, I couldn't fetch the weather.")

    elif "open github" in command:
        speak("Opening GitHub")
        webbrowser.open("https://github.com")

    elif "open linkedin" in command:
        speak("Opening LinkedIn")
        webbrowser.open("https://www.linkedin.com")

    elif "motivate me" in command:
        quotes = [
            "Believe you can and you're halfway there.",
            "Your limitation—it's only your imagination.",
            "Push yourself, because no one else is going to do it for you."
        ]
        speak(random.choice(quotes))

    elif "calculate" in command:
        try:
            expression = command.split("calculate")[-1].strip()
            result = eval(expression)
            speak(f"The result is {result}")
        except:
            speak("Sorry, I couldn't calculate that.")

    elif "exit" in command or "quit" in command:
        speak("Goodbye! See you later.")
        exit()

    # ------------------------
    # Additional Fun Commands
    # ------------------------
    elif "flip a coin" in command:
        speak(random.choice(["Heads", "Tails"]))

    elif "roll a dice" in command:
        speak(f"You rolled a {random.randint(1,6)}")

    elif "say hello to" in command:
        name = command.split("say hello to")[-1].strip()
        speak(f"Hello, {name}! Nice to meet you.")

    elif "fun fact" in command:
        facts = [
            "Honey never spoils.",
            "Bananas are berries, but strawberries aren't.",
            "Octopuses have three hearts.",
            "A day on Venus is longer than a year on Venus.",
            "Some cats are allergic to humans."
        ]
        speak(random.choice(facts))

    elif "spell" in command:
        word = command.split("spell")[-1].strip()
        spelled = ' '.join(list(word))
        speak(f"{word} is spelled as {spelled}")

    elif "remind me to" in command:
        task = command.split("remind me to")[-1].strip()
        speak(f"Reminder set: {task}")
        print(f"Reminder: {task}")

    elif "open calculator" in command:
        speak("Opening calculator")
        os.system("calc")  # Windows only

    elif "open notepad" in command:
        speak("Opening notepad")
        os.system("notepad")  # Windows only

    elif "sing a song" in command:
        songs = [
            "Twinkle, twinkle, little star, how I wonder what you are!",
            "Row, row, row your boat, gently down the stream!",
            "If you're happy and you know it, clap your hands!"
        ]
        speak(random.choice(songs))

    elif "shut down computer" in command:
        speak("Warning! The computer will shut down in 1 minute.")
        # os.system("shutdown /s /t 60")  # Uncomment to enable shutdown

    elif "motivate me 2.0" in command:
        quotes = [
            "Dream big and dare to fail.",
            "Keep going. Everything you need will come to you at the perfect time.",
            "Don't watch the clock; do what it does. Keep going.",
            "Success is not for the lazy."
        ]
        speak(random.choice(quotes))

    else:
        speak("Sorry, I didn't understand that command.")

# ------------------------
# Main Loop
# ------------------------
if __name__ == "__main__":
    speak("Hello! I am your AI Secretary. Type a command or 'exit' to quit.")
    while True:
        user_input = input("You: ")
        handle_command(user_input)
